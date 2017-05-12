package vn.vnpay.sms.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.pdu.SubmitSM;
import vn.vnpay.sms.smpp.ProcessorAbstract;
import vn.vnpay.sms.threadpool.ThreadPool;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;


public class MessageQueueSender
        implements Runnable {
    static Log logger = LogFactory.getLog(MessageQueueSender.class);

    private Hashtable processors = null;
    private boolean stopDequeue = false;
    private ThreadPool threadpool;

    /**
     * SubmitSmListener
     *
     * @param processors
     */
    public MessageQueueSender(Hashtable processors) {
        this.processors = processors;
        threadpool = new ThreadPool(1000);
    }

    /**
     * run
     */
    public void run() {
        SubmitSM sm;
        SendWorker sendWorker;

        while (!stopDequeue) {
            for (Enumeration e = processors.elements(); e.hasMoreElements(); ) {
                if (stopDequeue) break;
                ProcessorAbstract processor = (ProcessorAbstract) e.nextElement();
                if (processor.isBound()) {
                    try {
                        sm = processor.getDbworker().getQueueSubmitSM(processor.getGatewayId());
                        if (sm != null) {
                            sendWorker = new SendWorker(processor, sm);
                            threadpool.execute(sendWorker);
                            //processor.send(sm);
                        }
                    } catch (SQLException esql) {
                        logger.error(esql.getMessage());
                    } catch (InterruptedException e1) {
                        logger.error(e1.getMessage());
                    }
                }
            }
        }
    }

    /**
     * stop
     */
    public void stop() {
        stopDequeue = true;
        Thread.currentThread().interrupt();
    }

    private class SendWorker implements Runnable {
        private ProcessorAbstract processor;
        private SubmitSM sm;

        public SendWorker(ProcessorAbstract processor, SubmitSM sm) {
            this.processor = processor;
            this.sm = sm;
        }

        @Override
        public void run() {
            processor.send(sm);
        }
    }
}
