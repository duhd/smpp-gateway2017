/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package vn.vnpay.sms.receiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.Connection;
import org.smpp.Data;
import org.smpp.SmppObject;
import org.smpp.TCPIPConnection;
import vn.vnpay.db.OracleDatabasePooling;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.sql.SQLException;

public class SMSCListener extends SmppObject implements Runnable {
    private static Log logger = LogFactory.getLog(SMSCListener.class);
    private static String txt_receiver = "SMSCServer: ";
    private String queueName = "";
    private Connection serverConn = null;
    private int port;
    private long acceptTimeout = Data.ACCEPT_TIMEOUT;
    private long receiveTimeout = Data.RECEIVER_TIMEOUT;
    private PDUProcessorFactory processorFactory = null;
    private boolean keepReceiving = true;
    private boolean isReceiving = false;
    private boolean asynchronous = false;
    private OracleDatabasePooling pool = null;

    /**
     * Construct synchronous listener listening on the given port.
     *
     * @param port the port to listen on
     */
    public SMSCListener(int port) {
        this.port = port;
    }

    /**
     * Constructor with control if the listener starts as PropertiesConfig separate thread.
     * If <code>asynchronous</code> is true, then the listener is started
     * as PropertiesConfig separate thread, i.e. the creating thread can continue after
     * calling of method <code>start</code>. If it's false, then the
     * caller blocks while the listener does it's work, i.e. listening.
     *
     * @param port         the port to listen on
     * @param asynchronous if the listening will be performed as separate thread
     * @see #start()
     */
    public SMSCListener(int port, boolean asynchronous, long receiveTimeout, long acceptTimeout, String queueName) {
        this.port = port;
        this.asynchronous = asynchronous;
        this.receiveTimeout = receiveTimeout;
        this.acceptTimeout = acceptTimeout;
        this.queueName = queueName;
    }

    /**
     * Starts the listening. If the listener is asynchronous (reccomended),
     * then new thread is created which listens on the port and the
     * <code>start</code> method returns to the caller. Otherwise
     * the caller is blocked in the start method.
     *
     * @see #stop()
     */
    public synchronized void start()
            throws IOException {
        logger.info(txt_receiver + "going to start SMSCListener on port " + port);
        if (!isReceiving) {
            serverConn = new TCPIPConnection(port);
            serverConn.setReceiveTimeout(getAcceptTimeout());
            serverConn.open();
            keepReceiving = true;
            try {
                pool = new OracleDatabasePooling();
            } catch (SQLException e) {
                logger.error(txt_receiver + e.toString(), e);
            } catch (oracle.ucp.UniversalConnectionPoolException e) {
                logger.error(txt_receiver + e.toString(), e);
            }
            if (asynchronous) {
                logger.info(txt_receiver + "starting listener in separate thread.");
                Thread serverThread = new Thread(this);
                serverThread.start();
            } else {
                logger.info(txt_receiver + "going to listen in the context of current thread.");
                run();
            }
        } else {
            logger.info(txt_receiver + " already receiving, not starting the listener.");
        }
    }

    /**
     * Signals the listener that it should stop listening and wait
     * until the listener stops. Note that based on the timeout settings
     * it can take some time befor this method is finished -- the listener
     * can be blocked on i/o operation and only after exiting i/o
     * it can detect that it should stop.
     *
     * @see #start()
     */
    public synchronized void stop()
            throws IOException {
        logger.info(txt_receiver + "going to stop SMSCListener on port " + port);
        keepReceiving = false;
        while (isReceiving) {
            Thread.yield();
        }
        serverConn.close();

    }

    /**
     * The actual listening code which is run either from the thread
     * (for async listener) or called from <code>start</code> method
     * (for sync listener). The method can be exited by calling of method
     * <code>stop</code>.
     *
     * @see #start()
     * @see #stop()
     */
    public void run() {
        isReceiving = true;
        try {
            while (keepReceiving) {
                listen();
                Thread.yield();
            }
            logger.info(txt_receiver + "is stopped");
        } finally {
            isReceiving = false;
        }

    }

    /**
     * The "one" listen attempt called from <code>run</code> method.
     * The listening is atomicised to allow contoled stopping of the listening.
     * The length of the single listen attempt
     * is defined by <code>acceptTimeout</code>.
     * If PropertiesConfig connection is accepted, then new session is created on this
     * connection, new PDU processor is generated using PDU processor factory
     * and the new session is started in separate thread.
     *
     * @see #run()
     * @see org.smpp.Connection
     * @see SMSCSession
     * @see PDUProcessor
     * @see PDUProcessorFactory
     */
    private void listen() {
        try {
            Connection connection;
            serverConn.setReceiveTimeout(getAcceptTimeout());
            connection = serverConn.accept();

            if (connection != null) {
                SMSCSession session = new SMSCSession(connection);
                logger.info("SMSCListener accepted connection on port " + port + " from IpAddress " + connection.getAddress() + " and issued SessionId " + session.getSessionId());
                PDUProcessor pduProcessor = null;
                if (processorFactory != null) {
                    pduProcessor = processorFactory.createPDUProcessor(session);
                }
                try {
                    session.setDbConn(pool.getConnection());
                } catch (SQLException e) {
                    logger.error(txt_receiver + e.toString(), e);
                }
                session.setQueueName(queueName);
                session.setPDUProcessor(pduProcessor);
                session.setReceiveTimeout(receiveTimeout);

                Thread thread = new Thread(session);
                thread.setName(session.getSessionId());
                thread.start();
            } else {
                //logger.info(txt_receiver + "no connection accepted last "+getAcceptTimeout() +"ms");
            }
        } catch (InterruptedIOException e) {
            // thrown when the timeout expires => it's ok, we just didn't
            // receive anything
            logger.error(txt_receiver + "InterruptedIOException accepting, timeout? -> " + e);
        } catch (IOException e) {
            // accept can throw this from various reasons
            // and we don't want to continue then (?)
            logger.error(txt_receiver + "IOException accepting connection ->" + e);
            keepReceiving = false;
        }
    }

    /**
     * Sets PropertiesConfig PDU processor factory to use for generating PDU processors.
     *
     * @param processorFactory the new PDU processor factory
     */
    public void setPDUProcessorFactory(PDUProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    /**
     * Sets new timeout for accepting new connection.
     * The listening blocks the for maximum this time, then it
     * exits regardless the connection was acctepted or not.
     *
     * @param value the new value for accept timeout
     */
    public void setAcceptTimeout(int value) {
        acceptTimeout = value;
    }

    /**
     * Returns the current setting of accept timeout.
     *
     * @return the current accept timeout
     * @see #setAcceptTimeout(int)
     */
    public long getAcceptTimeout() {
        return acceptTimeout;
    }

}
