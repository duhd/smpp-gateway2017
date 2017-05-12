package vn.vnpay.sms.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import vn.vnpay.sms.smpp.ProcessorAbstract;


public class PDUReceiver
        implements Runnable {
    static Log logger = LogFactory.getLog(PDUReceiver.class);
    static SmsGatewayProps props = SmsGatewayProps.getInstance();


    ProcessorAbstract processor = null;
    private boolean exit = false;
    //private SmsGateway smsGateway = null;

    /**
     * constructor
     *
     * @param processor ProcessorGroup
     */
    public PDUReceiver(ProcessorAbstract processor) {
        this.processor = processor;
    }


    /**
     * run
     */
    public void run() {
        synchronized (this) {
            try {
                wait(3000);
            } catch (InterruptedException ignored) {
            }
        }
        while (!exit) {
            if (processor.isBound()) {
                processor.receive();
            } else {
                logger.warn("Forwarder: ["+processor.getGatewayId()+"] Restart Gateway...");
                synchronized (this) {
                    try {
                        processor.unbind();
                        try {
                            wait(props.getLong("smpp.wait-alive-interval", 60000));
                        } catch (InterruptedException ignored) {
                        }
                        processor.bind();
                    } catch (Exception e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }

    }

    /**
     * stop
     */
    public void stop() {
        exit = true;
        synchronized (this) {
            notify();
        }
    }
}
