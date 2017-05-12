package vn.vnpay.sms.smpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.SubmitSM;
import vn.vnpay.db.DatabaseWorker;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ProcessorAbstract {
    private static Log logger = LogFactory.getLog(ProcessorAbstract.class);

    protected String gatewayId;
    protected boolean concatenated_Mgs;
    protected boolean message_payload;
    protected int maxThrottling;
    protected final AtomicInteger requestCounter = new AtomicInteger();
    protected DatabaseWorker dbworker = null;
    protected boolean resendMaxThrottling;
    protected long alive_interval = 0;
    protected boolean deduplication = false;

    /**
     * ProcessorAbstract
     */
    public ProcessorAbstract() {
    }

    /**
     * Starts the daemon
     *
     * @throws Exception When error on starting up
     */
    public abstract void start()
            throws Exception;

    /**
     * Stops the daemon
     */
    public abstract void stop();

    /**
     * Receives message from SMSC and sends to deliver queue.
     *
     * @param msg DeliverMessage
     */
    public abstract void receive(DeliverSM msg);

    /**
     * Get message from submit queue and sends to SMSC.
     *
     * @param msg SubmitMessage
     */
    public abstract void send(SubmitSM msg);


    /**
     * Sends alive.
     */
    public abstract void sendAlive();

    /**
     * Check if the connection is bound.
     *
     * @return boolean
     */
    public abstract boolean isBound();


    public abstract String getBindType();

    public abstract void bind();

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setConcatenated_Mgs(boolean concatenated_Mgs) {
        this.concatenated_Mgs = concatenated_Mgs;
    }

    public void setMessage_payload(boolean message_payload) {
        this.message_payload = message_payload;
    }

    public void setMaxThrottling(int maxThrottling) {
        this.maxThrottling = maxThrottling;
    }

    public int getMaxThrottling() {
        return maxThrottling;
    }

    public void setRequestCounter(int requestCounter) {
        this.requestCounter.set(requestCounter);
    }

    public int getRequestCounterAndIncrement() {
        return this.requestCounter.getAndIncrement();
    }

    public int getRequestCounter() {
        return this.requestCounter.get();
    }

    public void setResendMaxThrottling(boolean resendMaxThrottling) {
        this.resendMaxThrottling = resendMaxThrottling;
    }

    public boolean getResendMaxThrottling() {
        return resendMaxThrottling;
    }

    public abstract void unbind();

    public abstract void receive();

    public abstract void setAliveInterval(long interval);

    public abstract long getAliveInterval();

    public abstract boolean getDeduplication();
    public abstract void setDeduplication(boolean deduplication);
    public abstract DatabaseWorker getDbworker();
}
