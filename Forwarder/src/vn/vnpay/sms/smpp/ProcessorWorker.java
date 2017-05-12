package vn.vnpay.sms.smpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.*;
import org.smpp.pdu.*;
import vn.vnpay.sms.threadpool.ThreadPool;

import java.io.IOException;


/**
 * Created by hoangdinhdu@gmail.com on 11/11/2014.
 */
public class ProcessorWorker extends SmppObject implements Runnable {
    private static Log logger = LogFactory.getLog(ProcessorWorker.class);
    private Session session = null;
    private boolean bound = false;
    private boolean keepRunning = true;
    private String ipAddress = "";
    private int port = 0;
    private String systemId = "";
    private String password = "";
    private String bindOption = "";
    private boolean asynchronous = true;
    private AddressRange addressRange = new AddressRange();
    private String systemType = "smpp";
    /*
     * for information about these variables have a look in SMPP 3.4
     * specification
     */
    String messageId = "";
    private PDUEventListener pduListener;

    private SmppProcessor processor = null;

    long receiveTimeout = Data.RECEIVE_BLOCKING;

    public ProcessorWorker(SmppProcessor processor) {
        this.processor = processor;
        bindOption = processor.getBindType();
        systemId = processor.getUser();
        password = processor.getPassword();
        port = processor.getPort();
        ipAddress = processor.getHost();
        systemType = processor.getSystemType();
        try {
            addressRange.setAddressRange(processor.getAddressRange());
            addressRange.setTon((byte) processor.getTon());
            addressRange.setNpi((byte) processor.getNpi());
        } catch (WrongLengthOfStringException e) {
            logger.error(e.getMessage());
        }
    }

    public void stop() {
        if (bound) {
            unbind();
        }
        keepRunning = false;
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void run() {
        new TrafficWatcherThread().start();
        while (keepRunning) {
            bind();
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void bind() {
        try {

            if (bound) {
                logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Already bound, unbind first.");
                return;
            }

            BindRequest request;
            BindResponse response;


            if (bindOption.compareToIgnoreCase("t") == 0) {
                request = new BindTransmitter();
            } else if (bindOption.compareToIgnoreCase("r") == 0) {
                request = new BindReceiver();
            } else if (bindOption.compareToIgnoreCase("tr") == 0) {
                request = new BindTransciever();
            } else {
                logger.error(txt_forwarder + "[" + processor.getGatewayId() + "]" +
                        "Invalid bind mode, expected t, r or tr, got " + bindOption + ". Operation canceled.");
                return;
            }

            TCPIPConnection connection = new TCPIPConnection(ipAddress, port);
            connection.setReceiveTimeout(20 * 1000);
            session = new Session(connection);

            // set values
            request.setSystemId(systemId);
            request.setPassword(password);
            request.setSystemType(systemType);
            request.setInterfaceVersion((byte) 0x34);
            request.setAddressRange(addressRange);

            // send the request
            logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Bind request " + request.debugString());
            if (asynchronous) {
                pduListener = new PDUEventListener(session);
                response = session.bind(request, pduListener);
            } else {
                response = session.bind(request);
            }
            logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Bind response " + response.debugString());
            if (response.getCommandStatus() == 0) {
                bound = true;
            }
        } catch (Exception e) {
            logger.error(txt_forwarder + "[" + processor.getGatewayId() + "] Bind operation failed. " + e.toString());
        }
    }

    public void unbind() {
        try {

            if (!bound) {
                logger.warn(txt_forwarder + "[" + processor.getGatewayId() + "] Not bound, cannot unbind.");
                return;
            }

            UnbindResp response = session.unbind();
            logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Unbind response " + response.debugString());
            bound = false;

        } catch (Exception e) {
            bound = false;
            try {
                session.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (WrongSessionStateException e1) {
                e1.printStackTrace();
            }
            logger.error(txt_forwarder + "[" + processor.getGatewayId() + "] Unbind operation failed: " + e.toString());
            //e.printStackTrace();
        }
    }

    public void send(SubmitSM sm) {
        if (processor.getMaxThrottling() == 0 || processor.getRequestCounter() < processor.getMaxThrottling()) {
            submit(sm);
            processor.getRequestCounterAndIncrement();
        } else {
            logger.warn(txt_forwarder + "[" + processor.getGatewayId() + "] " + "Current Throttling is " + processor.getRequestCounter() + ", over MaxThrottling!");
            try {
                Thread.sleep(500);
                send(sm);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void submit(SubmitSM sm) {
        SubmitSMResp response;
        try {
            logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] " +
                    "Sending cmd (SUBMIT_SM): Msg_id=" + sm.getSequenceNumber() + ";SourceAddr=" + sm.getSourceAddr().getAddress() + ";DestAddr=" + sm.getDestAddr().getAddress() + ";Text=" + sm.getShortMessage());
            if (asynchronous) {
                session.submit(sm);
            } else {
                response = session.submit(sm);
                messageId = response.getMessageId();
                //Update tbl_Submit here...
            }
        } catch (Exception e) {
            logger.warn(txt_forwarder + "[" + processor.getGatewayId() + "] Submit operation failed. " + e.getMessage());
            logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Retry binding..");
            unbind();
            bind();
            this.submit(sm);
        }
    }

    public void enquireLink() {
        try {

            EnquireLink request = new EnquireLink();
            EnquireLinkResp response;
            logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Enquire Link request " + request.debugString());
            if (asynchronous) {
                session.enquireLink(request);
            } else {
                response = session.enquireLink(request);
                logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Enquire Link response " + response.debugString());
            }

        } catch (Exception e) {
            logger.warn(txt_forwarder + "[" + processor.getGatewayId() + "] Enquire Link operation failed. " + e.getMessage());
            logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Retry binding...");
            unbind();
            bind();
        }
    }

    public void receive() {
        try {

            PDU pdu = null;
            //System.out.print("Going to receive a PDU. ");
            /*if (receiveTimeout == Data.RECEIVE_BLOCKING) {
                System.out.print(
                        "The receive is blocking, i.e. the application " + "will stop until a PDU will be received.");
            } else {
                System.out.print("The receive timeout is " + receiveTimeout / 1000 + " sec.");
            }
            logger.info();*/
            if (asynchronous) {
                ServerPDUEvent pduEvent = pduListener.getRequestEvent(receiveTimeout);
                if (pduEvent != null) {
                    pdu = pduEvent.getPDU();
                }
            } else {
                pdu = session.receive(receiveTimeout);
            }
            if (pdu != null) {

                if (pdu.isRequest()) {
                    logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Received request PDU " + pdu.debugString());
                    switch (pdu.getCommandId()) {
                        case Data.ENQUIRE_LINK:
                            //logger.debug(txt_forwarder + "[" + processor.getGatewayId() + "] Update to table GATEWAY_STATUS here ...");
                            break;
                        case Data.DELIVER_SM:
                            DeliverSM deliverSM = (DeliverSM) pdu;
                            if (deliverSM.getEsmClass() == 0x4) {
                                //logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Update DLR to log table SUBMIT_SM here ...");
                                processor.dbworker.updateDeliverMessage(deliverSM);
                            } else {
                                //processor.dbworker.updateDeliverMessage(deliverSM);
                                processor.dbworker.insertQueueMO(processor.getGatewayId(), deliverSM.getSourceAddr().getAddress(), deliverSM.getDestAddr().getAddress(), deliverSM.getShortMessage(), deliverSM.getSequenceNumber());
                                //logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Insert to log table DELIVER_SM and Insert to MO_Queue here ...");
                            }
                            break;
                    }
                    Response response = ((Request) pdu).getResponse();
                    // respond with default response
                    logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Response PDU " + response.debugString());
                    session.respond(response);
                } else if (pdu.isResponse()) {
                    logger.info(txt_forwarder + "[" + processor.getGatewayId() + "] Received response PDU " + pdu.debugString());
                    switch (pdu.getCommandId()) {
                        case Data.SUBMIT_SM_RESP:
                            //logger.debug(txt_forwarder + "[" + processor.getGatewayId() + "] Update to log table SUBMIT_SM here ...");
                            SubmitSMResp smResp = (SubmitSMResp) pdu;
                            processor.dbworker.updateSubmitResp(smResp.getSequenceNumber(), smResp.getMessageId(), smResp.getCommandStatus(), processor.getGatewayId());
                            break;
                        case Data.ENQUIRE_LINK_RESP:
                            //logger.debug(txt_forwarder + "[" + processor.getGatewayId() + "] Update to table GATEWAY_STATUS here ...");
                            EnquireLinkResp enquireLinkResp = (EnquireLinkResp) pdu;
                            if (enquireLinkResp.getCommandStatus() == 0)
                                processor.dbworker.updateEnquireLinkResp(processor.getGatewayId(), enquireLinkResp.getSequenceNumber());
                            break;
                        case Data.UNBIND:
                            session.close();
                            logger.debug(txt_forwarder + "[" + processor.getGatewayId() + "] Request UNBIND from SMSC, session closing...");
                            break;
                    }
                }
            } else {
                //logger.info("No PDU received this time.");
            }

        } catch (Exception e) {
            logger.error(txt_forwarder + "[" + processor.getGatewayId() + "] Receiving failed. " + e.getMessage());
        }
    }


    public boolean isBound() {
        return bound;
    }

    private class TrafficWatcherThread extends Thread {
        private ThreadPool threadpool = null;
        @Override
        public void run() {
            Thread.currentThread().setName("Traffic-Watcher-Thread");
            logger.info("Starting traffic watcher...");
            threadpool = new ThreadPool(30);
            while (keepRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn(e.toString(), e);
                }

                int smpp_processor = processor.getRequestCounter();
                if (smpp_processor > 0) {
                    logger.debug(txt_forwarder + "[" + processor.getGatewayId() + "] Messages per second: " + smpp_processor);
                    try {
                        WriteDBWorker writeDBWorker = new WriteDBWorker(smpp_processor, processor.getGatewayId(), System.currentTimeMillis());
                        threadpool.execute(writeDBWorker);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    processor.setRequestCounter(0);
                }
            }
        }

    }

    private class WriteDBWorker implements Runnable {
        private int traffic;
        private String gateway;
        private long timeMillis;


        public WriteDBWorker(int traffic, String gateway, long timeMillis) {
            this.traffic = traffic;
            this.gateway = gateway;
            this.timeMillis = timeMillis;
        }

        @Override
        public void run() {
            try {
                processor.dbworker.insertMessageCounterPerSecond(traffic, gateway, timeMillis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
