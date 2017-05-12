package vn.vnpay.sms.smpp;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.SubmitSM;
import vn.vnpay.db.DatabaseWorker;

import java.sql.Connection;

public class SmppProcessor
        extends ProcessorAbstract {
    private static Log logger = LogFactory.getLog(SmppProcessor.class);
    private String host;
    private int port;
    private String user;
    private String password;
    private String systemType;
    private String bindType;
    private int ton;
    private int npi;
    private String addressRange;
    private Connection dbConn;

    private ProcessorWorker worker = null;
    private String queueName = "";

    public SmppProcessor(Connection dbConn,
                         String queuename,
                         String host,
                         int port,
                         String user,
                         String password,
                         String systemType,
                         String bindType,
                         int ton,
                         int npi,
                         String addressRange
    ) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.systemType = systemType;
        this.bindType = bindType;
        this.ton = ton;
        this.npi = npi;
        this.addressRange = addressRange;
        this.dbConn = dbConn;
        this.queueName =queuename;
    }

    /**
     * Starts the daemon.
     *
     * @throws Exception When error on starting up
     */
    public void start()
            throws Exception {
        dbworker = new DatabaseWorker(dbConn);
        dbworker.setQueueName(queueName);
        if (worker == null) {
            worker = new ProcessorWorker(this);
            Thread workerThread = new Thread(worker, gatewayId + ":smpp-client");
            workerThread.start();
        } else {
            logger.info("Forwarder: [" + gatewayId + "] already bind!");
        }
    }


    /**
     * Stops the daemon.
     */
    public void stop() {
        if (worker != null) {
            worker.stop();
            worker = null;
        }
    }


    /**
     * Receives message from SMSC and sends to deliver queue.
     *
     * @param dm ReponseSM
     */
    public void receive(DeliverSM dm) {
        logger.info("Receive message:\n" + dm.debugString());
        try {
            dbworker.updateDeliverMessage(dm);
        } catch (Exception e) {
            logger.warn(e.toString(), e);
        }
    }


    /**
     * Get message from QueueMessage and sends to SMSC.
     *
     * @param qm QueueMessage
     */
    public void send(SubmitSM qm) {
        // logger.info("Send message:\n" + sm);
        try {
            //DatabaseWrapper.update(qm);
            if (worker != null && worker.isBound()) {
                worker.send(qm);
            }
        } catch (Exception e) {
            logger.warn(e.toString(), e);
        }
    }

    /**
     * Sends alive.
     */
    public void sendAlive() {
        if (worker != null && worker.isBound()) {
            worker.enquireLink();
        }
    }

    /**
     * Check if the connection is bound.
     *
     * @return boolean
     */
    public boolean isBound() {
        return worker.isBound();
    }


    public String getAddressRange() {
        return addressRange;
    }


    public String getHost() {
        return host;
    }


    public int getNpi() {
        return npi;
    }


    public String getPassword() {
        return password;
    }


    public int getPort() {
        return port;
    }


    public String getSystemType() {
        return systemType;
    }


    public int getTon() {
        return ton;
    }


    public String getUser() {
        return user;
    }


    public void setAddressRange(String addressRange) {
        this.addressRange = addressRange;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public void setNpi(int npi) {
        this.npi = npi;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }


    public void setTon(int ton) {
        this.ton = ton;
    }


    public void setUser(String user) {
        this.user = user;
    }

    public String getBindType() {
        return bindType;
    }

    public void setBindType(String bindType) {
        this.bindType = bindType;
    }

    public void bind() {
        worker.bind();
    }

    public void unbind() {
        worker.unbind();
    }

    public void receive() {
        worker.receive();
    }

    public void setAliveInterval(long interval) {
        this.alive_interval = interval;
    }

    public long getAliveInterval() {
        return this.alive_interval;
    }

    @Override
    public boolean getDeduplication() {
        return this.deduplication;
    }

    @Override
    public void setDeduplication(boolean deduplication) {
        this.deduplication = deduplication;
    }

    @Override
    public DatabaseWorker getDbworker() {
        return this.dbworker;
    }


}
