package vn.vnpay.sms.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import vn.vnpay.db.OracleDatabasePooling;
import vn.vnpay.sms.smpp.ProcessorAbstract;
import vn.vnpay.sms.smpp.SmppProcessor;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

@SuppressWarnings("unchecked")
public class SmsGateway extends Thread {
    static Log logger = LogFactory.getLog(SmsGateway.class);
    static SmsGatewayProps props = SmsGatewayProps.getInstance();
    boolean started = false;
    private Hashtable processors = null;
    private AliveSender aliveSender = null;
    private PDUReceiver PDUReceiver = null;
    private MessageQueueSender messageQueueSender = null;
    private java.sql.Connection dbConn = null;
    private OracleDatabasePooling pool = null;

    /**
     * SmsGateway
     */
    public SmsGateway() {
    }

    /**
     * init
     *
     * @throws Exception
     */
    public void init() {
        try {
            //Khoi tao DBA Pool
            try {
                pool = new OracleDatabasePooling();
            } catch (SQLException e) {
                logger.error("Forwarder: " + e.toString(), e);
            } catch (oracle.ucp.UniversalConnectionPoolException e) {
                logger.error("Forwarder: " + e.toString(), e);
            }

            // initializes smpp processors
            processors = new Hashtable();
            String[] smpp_gatewayIds = props.getStringArray(SmsGatewayProps.SMPP_GATEWAY_ID);
            for (int i = 0; i < smpp_gatewayIds.length && smpp_gatewayIds[i].length() > 0; i++) {
                dbConn = pool.getConnection();
                ProcessorAbstract processor = new SmppProcessor(dbConn,
                        props.getString(smpp_gatewayIds[i] + SmsGatewayProps.QUEUE_NAME),
                        props.getString(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_HOST_POSTFIX),
                        props.getInt(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_PORT_POSTFIX),
                        props.getString(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_USER_POSTFIX),
                        props.getString(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_PASSWORD_POSTFIX),
                        props.getString(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_SYSTEM_TYPE_POSTFIX),
                        props.getString(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_BIND_TYPE_POSTFIX, "tr"),
                        props.getInt(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_TON_POSTFIX),
                        props.getInt(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_NPI_POSTFIX),
                        props.getString(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_ADDRESS_RANGE_POSTFIX)
                );
                processor.setGatewayId(smpp_gatewayIds[i]);
                processor.setAliveInterval(props.getLong(SmsGatewayProps.SMPP_ALIVE_INTERVAL, 45000));
                processor.setDeduplication(props.getBoolean(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_DEDUPLICATION, false));
                processor.setMessage_payload(props.getBoolean(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_MESSAGE_PAYLOAD, Boolean.TRUE));
                processor.setMaxThrottling(props.getInt(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_MAX_THROTTLLING, 0));
                processor.setResendMaxThrottling(props.getBoolean(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_RESEND_MAX_THROTTLLING, true));
                processor.setConcatenated_Mgs(props.getBoolean(smpp_gatewayIds[i] + SmsGatewayProps.SMPP_CONCATENATED_SUPPORT, true));
                processors.put(smpp_gatewayIds[i], processor);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * start
     */
    public void start() {
        init();
        if (!started) {
            try {
                // start smpp processors
                for (Enumeration e = processors.elements(); e.hasMoreElements(); ) {
                    ProcessorAbstract processor = (ProcessorAbstract) e.nextElement();
                    processor.start();
                    Thread.sleep(100);
                }
                for (Enumeration e = processors.elements(); e.hasMoreElements(); ) {
                    ProcessorAbstract processor = (ProcessorAbstract) e.nextElement();
                    //creates PDUReceiver
                    PDUReceiver = new PDUReceiver(processor);
                    //start PDUReceiver
                    new Thread(PDUReceiver, "PDUReceiver-" + processor.getGatewayId()).start();
                }
                // creates alive sender deamon
                aliveSender = new AliveSender(processors);
                // start alive sender
                new Thread(aliveSender, "Alive-sender").start();

                // creates submit message listener
                messageQueueSender = new MessageQueueSender(processors);
                // start listener for submit messages.
                new Thread(messageQueueSender, "MessageQueueSender").start();

                started = true;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * stop
     */
    public int stop(int exitCode) {

        // stop listener
        messageQueueSender.stop();

        // stop alive sender
        aliveSender.stop();

        //stop PDUReceiver request from SMSC
        PDUReceiver.stop();

        // stop processors
        for (Enumeration e = processors.elements(); e.hasMoreElements(); ) {
            ProcessorAbstract processor = (ProcessorAbstract) e.nextElement();
            processor.stop();
        }

        started = false;
        // stop DBA Poll
        try {
            dbConn.close();
        } catch (SQLException e) {
            logger.error("Forwarder: " + e.toString(), e);
        }
        logger.info("SMS Gateway Stopped!");
        return exitCode;
    }


    /**
     * main
     *
     * @param args String[]
     * @throws Exception
     */
    public static void main(String[] args)
            throws Exception {
        logger.info("SMS Gateway Start!-----------------------------------------------------------------------------------------------");
        SmsGateway gateway = new SmsGateway();
        Runtime.getRuntime().addShutdownHook(new SmsGatewayShutdown(gateway));
        gateway.start();
    }

}

