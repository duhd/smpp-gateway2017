package vn.vnpay.sms.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import vn.vnpay.sms.smpp.ProcessorAbstract;

import java.util.Enumeration;
import java.util.Hashtable;


public class AliveSender
        implements Runnable {
    static Log logger = LogFactory.getLog(AliveSender.class);
    static SmsGatewayProps props = SmsGatewayProps.getInstance();


    Hashtable processors = null;
    private boolean exit = false;
    int count = 0;
    //private SmsGateway smsGateway = null;

    /**
     * constructor
     *
     * @param processors ProcessorGroup
     */
    public AliveSender(Hashtable processors) {
        this.processors = processors;
    }


    /**
     * run
     */
    public void run() {
        while (!exit) {

            synchronized (this) {
                try {
                    wait(props.getLong("smpp.wait-alive-interval", 45000));
                } catch (InterruptedException ignored) {
                }
            }
            if (!exit) {

//                if (count >= 10000) exit = true;
//                else count++;
//                for (Enumeration e = processors.elements(); e.hasMoreElements(); ) {
//                    ProcessorAbstract processor = (ProcessorAbstract) e.nextElement();
//                    processor.send(submitSMTest());
//                }


                for (Enumeration e = processors.elements(); e.hasMoreElements(); ) {
                    if (exit) break;
                    ProcessorAbstract processor = (ProcessorAbstract) e.nextElement();
                    if (processor.isBound() && processor.getAliveInterval() > 0) {
                        processor.sendAlive();
                    } else {
                        logger.error("[" + processor.getGatewayId() + "] Sending cmd (ENQUIRE_LINK): Failed, not bind");
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
