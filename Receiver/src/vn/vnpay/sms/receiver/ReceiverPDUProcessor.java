package vn.vnpay.sms.receiver;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.Data;
import org.smpp.pdu.*;
import vn.vnpay.db.SubmitSM_Insert;
import vn.vnpay.db.TypeEnqueueSm;
import vn.vnpay.sms.receiver.threadpool.ThreadPool;

import java.sql.SQLException;


public class ReceiverPDUProcessor extends PDUProcessor {
    private static Log logger = LogFactory.getLog(ReceiverPDUProcessor.class);

    /**
     * The session this processor uses for sending of PDUs.
     */
    private SMSCSession session = null;
    private String ipAddress = null;



    /**
     * Indicates if the bound has passed.
     */
    private boolean bound = false;

    /**
     * The system id of the bounded ESME.
     */

    /**
     * The message id assigned by simulator to submitted messages.
     */
    private static int intMessageId = 2000;

    /**
     * System id of this simulator sent to the ESME in bind response.
     */
    private static final String SYSTEM_ID = "VNPAY-EMSC";

    /**
     * The name of attribute which contains the system id of ESME.
     */
    private static final String SYSTEM_ID_ATTR = "name";

    /**
     * The name of attribute which contains password of ESME.
     */
    private static final String PASSWORD_ATTR = "password";
    private ThreadPool threadpool = null;

    /**
     * Constructs the PDU processor with given session,
     * message store for storing of the messages and PropertiesConfig table of
     * users for authentication.
     *
     * @param session the sessin this PDU processor works for
     */
    public ReceiverPDUProcessor(SMSCSession session) {
        this.session = session;
        this.ipAddress = session.getIpAddress();
        threadpool = new ThreadPool(100);
    }

    /**
     * Depending on the <code>commandId</code>
     * of the <code>request</code> creates the proper response.
     * The first request must be PropertiesConfig <code>BindRequest</code> with the correct
     * parameters.
     *
     * @param request the request from client
     */
    public void clientRequest(Request request) {
        Response response;
        int commandStatus;
        int commandId = request.getCommandId();
        try {
            //bound = true; // không cần bind - duhd
            if (!bound) { // the first PDU must be bound request
                if (commandId == Data.BIND_TRANSMITTER ||
                        commandId == Data.BIND_RECEIVER ||
                        commandId == Data.BIND_TRANSCEIVER) {
                    commandStatus = checkIdentity((BindRequest) request, ipAddress, commandId);
                    if (commandStatus == 0) { // authenticated
                        // firstly generate proper bind response
                        BindResponse bindResponse =
                                (BindResponse) request.getResponse();
                        bindResponse.setSystemId(SYSTEM_ID);
                        bindResponse.setScInterfaceVersion((byte) 0x34);
                        // and send it to the client via serverResponse
                        serverResponse(bindResponse);
                        // success => bound
                        bound = true;
                    } else { // system id not authenticated
                        // get the response
                        response = request.getResponse();
                        // set it the error command status
                        response.setCommandStatus(commandStatus);
                        // and send it to the client via serverResponse
                        serverResponse(response);
                        // bind failed, stopping the session
                        session.stop();
                    }
                } else {
                    if (request.canResponse()) {
                        // get the response
                        response = request.getResponse();
                        response.setCommandStatus(Data.ESME_RINVBNDSTS);
                        // and send it to the client via serverResponse
                        serverResponse(response);
                    }
                    // bind failed, stopping the session
                    session.stop();
                }
            } else { // already bound, can receive other PDUs
                if (request.canResponse()) {
                    response = request.getResponse();
                    switch (commandId) { // for selected PDUs do extra steps
                        case Data.SUBMIT_SM:
                            SubmitSMResp submitResponse = (SubmitSMResp) response;
                            try {
                                SubmitSM sm = (SubmitSM) request;
                                TypeEnqueueSm status_sm = session.getDbworker().enqueueSubmitSM(sm,session.getUsername());
                                submitResponse.setMessageId(assignMessageId(status_sm.getMsg_id()));
                                submitResponse.setCommandStatus(status_sm.getCmd_status());
                                serverResponse(submitResponse);
                                //logger.info("[" + session.getUsername() + "/" + ipAddress + "] PDUProcessor (SUBMIT_SM): " +
                                //        "Seq=" + sm.getSequenceNumber() +
                                //        ";EsmClass=" + sm.getEsmClass() + ";DataCode=" + sm.getDataCoding() + ";SourceAddr=" + sm.getSourceAddr().getAddress() + ";DestAddr=" + sm.getDestAddr().getAddress() +
                                //        ";Message=" + sm.getShortMessage() + ";Response Status=" + submitResponse.getCommandStatus()+ ";Response MessageId=" + submitResponse.getMessageId());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                            break;

                        case Data.ENQUIRE_LINK:
                            serverResponse(response);
                            break;

                        case Data.UNBIND:
                            serverResponse(response);
                            session.stop();
                            break;
                        case Data.BIND_TRANSMITTER:
                            response.setCommandStatus(Data.ESME_ROK);
                            serverResponse(response);
                            break;
                        case Data.BIND_RECEIVER:
                            response.setCommandStatus(Data.ESME_ROK);
                            serverResponse(response);
                            break;
                        case Data.BIND_TRANSCEIVER:
                            response.setCommandStatus(Data.ESME_ROK);
                            serverResponse(response);
                            break;
                        default:
                            logger.warn("[" + session.getUsername() + "/" + ipAddress + "] " +
                                    "CommandId = 0x" + Integer.toHexString(commandId) + " not support!");
                            response.setCommandStatus(Data.ESME_RINVCMDID);
                            serverResponse(response);
                    }
                } else {
                    logger.warn("[" + session.getUsername() + "/" + ipAddress + "] " +
                            "Unexpected packet received! Id = 0x" + Integer.toHexString(commandId));
                }
            }
        } catch (WrongLengthOfStringException e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * Processes the response received from the client.
     *
     * @param response the response from client
     */
    public void clientResponse(Response response) {
        logger.info("[" + session.getUsername() + "/" + ipAddress + "] Client Response: "//+ response.getData().getHexDump() + "]\n"
                + response.debugString());
    }

    /**
     * @param request the request to be sent to the client
     */
    public void serverRequest(Request request) {
        logger.info("[" + session.getUsername() + "/" + ipAddress + "] Server Request: " +//+ request.getData().getHexDump() + "]\n" +
                request.debugString());
        session.send(request);
    }

    /**
     * Send the response created by <code>clientRequest</code> to the client.
     *
     * @param response the response to send to client
     */
    public void serverResponse(Response response) {
        //debug.write("ReceiverPDUProcessor.serverResponse() " + response.debugString());
        //display("server response: " + response.debugString());
        logger.info("[" + session.getUsername() + "/" + ipAddress + "] Server Response: " //+ response.getData().getHexDump() + "]\n" +
                + response.debugString());
        session.send(response);
    }

    /**
     * Checks if the bind request contains valid system id and password.
     * For this uses the table of users provided in the constructor of the
     * <code>ReceiverPDUProcessor</code>. If the authentication fails,
     * i.e. if either the user isn't found or the password is incorrect,
     * the function returns proper status code.
     *
     * @param request   the bind request as received from the client
     * @param ipAddress
     * @param commandId
     * @return status code of the authentication; ESME_ROK if authentication
     * passed
     */


    private int checkIdentity(BindRequest request, String ipAddress, int commandId) {
        int commandStatus;
        systemId = request.getSystemId();
        session.setUsername(systemId);
        if (session.getDbworker().authenticated(systemId, request.getPassword(), getSessionId(), ipAddress, commandId)) {
            commandStatus = Data.ESME_ROK;
        } else {
            commandStatus = Data.ESME_RINVSYSID;
            logger.warn("[" + session.getUsername() + "/" + ipAddress + "] Authenticate Failed, status=" + commandStatus);
        }

        return commandStatus;
    }

    /**
     * Creates PropertiesConfig unique message_id for each sms sent by PropertiesConfig client to the smsc.
     *
     * @return unique message id
     */
    private String assignMessageId() {
        String messageId = "vnpay";
        intMessageId++;
        messageId += intMessageId;
        return messageId;
    }

    private String assignMessageId(int msgid) {
        String messageId = "vnpay";
        messageId += msgid;
        return messageId;
    }

    /**
     * Returns the session this PDU processor works for.
     *
     * @return the session of this PDU processor
     */
    public SMSCSession getSession() {
        return session;
    }

    /**
     * Returns the system id of the client for whose is this PDU processor
     * processing PDUs.
     *
     * @return system id of client
     */
    public String getSystemId() {
        return systemId;
    }

    public String getSessionId() {
        return session.getSessionId();
    }

    private class WriteDBWorker implements Runnable {
        private SubmitSM sm;
        private SubmitSMResp response;
        private String gatewayId;
        private boolean deduplication;

        public WriteDBWorker(SubmitSM sm, SubmitSMResp response, String gatewayId, boolean deduplication) {
            this.sm = sm;
            this.response = response;
            this.gatewayId = gatewayId;
            this.deduplication = deduplication;
        }

        @Override
        public void run() {
            int commandStatus = 0;
            try {
                SubmitSM_Insert result = session.getDbworker().insertSubmitSM(gatewayId, sm.getSequenceNumber(),
                        sm.getSourceAddr().getAddress(), sm.getSourceAddr().getTon(), sm.getSourceAddr().getNpi(),
                        sm.getDestAddr().getAddress(), sm.getDestAddr().getTon(), sm.getDestAddr().getNpi(),
                        sm.getEsmClass(), sm.getDataCoding(), sm.getShortMessage(), commandStatus, deduplication);
                response.setMessageId(assignMessageId(result.getMessage_id()));
                response.setCommandStatus(result.getCommand_status());
                commandStatus = result.getCommand_status();
                logger.info("[" + session.getUsername() + "/" + ipAddress + "] Received (SUBMIT_SM): " +
                        "Seq=" + sm.getSequenceNumber() +
                        ";EsmClass=" + sm.getEsmClass() + ";DataCode=" + sm.getDataCoding() + ";SourceAddr=" + sm.getSourceAddr().getAddress() + ";DestAddr=" + sm.getDestAddr().getAddress() +
                        ";Message=" + sm.getShortMessage() + ";MessageId=" + response.getMessageId() + ";CommandStatus=" + commandStatus);
                serverResponse(response);
            } catch (SQLException e1) {
                e1.printStackTrace();
            } catch (WrongLengthOfStringException e) {
                e.printStackTrace();
            }
        }

    }
}
