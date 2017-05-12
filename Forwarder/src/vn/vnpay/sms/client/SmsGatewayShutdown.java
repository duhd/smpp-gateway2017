package vn.vnpay.sms.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SmsGatewayShutdown
        extends Thread {
    static Log logger = LogFactory.getLog(SmsGateway.class);
    private final SmsGateway gateway;

    public SmsGatewayShutdown(SmsGateway gateway) {
        this.gateway=gateway;
    }

    /**
     * run
     */
    public void run() {
        logger.info("SMS Gateway going to stop!");
        gateway.stop(0);
    }

}
