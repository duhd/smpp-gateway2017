package vn.vnpay.sms.receiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SMSCShutdown
        extends Thread {
    private static Log logger = LogFactory.getLog(SMSCShutdown.class);
    private final SMSCServer smscServer;

    public SMSCShutdown(SMSCServer smscServer) {
        this.smscServer = smscServer;
    }

    /**
     * run
     */
    public void run() {
        logger.info("SMSCServer: going to stop...");
        smscServer.stop(0);
    }
}
