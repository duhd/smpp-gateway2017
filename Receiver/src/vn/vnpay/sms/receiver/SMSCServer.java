package vn.vnpay.sms.receiver;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.SmppObject;

import java.io.IOException;
import java.util.Enumeration;


public class SMSCServer extends SmppObject {
    private static PropertiesConfig config = PropertiesConfig.getInstance();
    private static Log logger = LogFactory.getLog(SMSCServer.class);
    private static String txt_receiver = "SMSCServer: ";

    private static int list_port = config.getInt(PropertiesConfig.LISTEN_PORT, 8888);
    private static long receiveTimeout = config.getLong(PropertiesConfig.RECEIVER_TIMEOUT, 60000);
    private static long acceptTimeout = config.getLong(PropertiesConfig.ACCEPT_TIMEOUT, 10000);
    private static String queueName = config.getString(PropertiesConfig.QUEUE_NAME, "QUEUE_SUBMIT_SM");

    boolean keepRunning = true;
    private SMSCListener smscListener = null;
    private PDUProcessorGroup processors = null;

    public SMSCServer() {
    }

    public static void main(String[] args)
            throws Exception {
        logger.info("SMSCServer going to start......................................................................!");
        SMSCServer smscServer = new SMSCServer();
        Runtime.getRuntime().addShutdownHook(new SMSCShutdown(smscServer));
        smscServer.start();

    }


    public void start() {
        if (smscListener == null)
            try {
                logger.info("Starting listener... ");
                smscListener = new SMSCListener(list_port, true, receiveTimeout, acceptTimeout, queueName);
                processors = new PDUProcessorGroup();
                ReceiverPDUProcessorFactory factory = new ReceiverPDUProcessorFactory();
                smscListener.setPDUProcessorFactory(factory);
                smscListener.start();
            } catch (Exception ex) {
                logger.error(txt_receiver + "..start failed.");
            }
        else {
            logger.warn(txt_receiver + "Listener is already running.");
        }
    }

    public int stop(int i) {
        if (smscListener != null) {
            SMSCSession session;
            synchronized (processors) {
                for (Enumeration e = processors.elements(); e.hasMoreElements(); ) {
                    ReceiverPDUProcessor processor = (ReceiverPDUProcessor) e.nextElement();
                    session = processor.getSession();
                    logger.info(txt_receiver + "Stopping session " + session.getSessionId() + ": " +
                            session.getIpAddress() + " ...");
                    session.stop();
                }
            }
            keepRunning = false;
            try {
                logger.info(txt_receiver + "Stopping listener on port " + list_port);
                smscListener.stop();
            } catch (IOException e) {
                logger.error(e.toString(), e);
            }
            smscListener = null;
        }
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

}