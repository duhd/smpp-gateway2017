package vn.vnpay.db;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smpp.pdu.*;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.sql.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DatabaseWorker {
    static Log logger = LogFactory.getLog(DatabaseWorker.class);
    private Connection cn = null;
    private String serverID = "";

    private String queueName = "";
    private static OracleDatabasePooling pool;

    public DatabaseWorker(Connection conn) {
        this.cn = conn;
        try {
            serverID = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void stop() {
        logger.info("Close the connection pooling");
        try {
            cn.close();
            pool.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public SubmitSM_Insert insertSubmitSM(String gatewayId, int seqRequest, String sourceAddr, byte sourceTon, byte sourceNpi, String destAddr, byte destTon, byte destNpi,
                                          byte esmClass, byte dataCoding, String shortMessage, int commandStatus, boolean deduplication)
            throws SQLException {
        CallableStatement cs = null;
        SubmitSM_Insert result = new SubmitSM_Insert(0, 0);
        try {
            //cs = cn.prepareCall("{call  INSERT_SUBMIT_SM(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            cs = cn.prepareCall("{call  SMSQUEUE.INSERT_SUBMIT_SM ( ?,? ,?, ? , ? ,? ,? ,?  ,? ,? ,? ,?  ,? , ?  , ? )}");
            cs.setString(1, gatewayId);//p_GATEWAY_ID
            cs.setInt(2, seqRequest);//p_SEQ_REQUEST
            cs.setString(3, sourceAddr);//p_SOURCE_ADDR
            cs.setInt(4, sourceTon);//p_SOURCE_ADDR_TON
            cs.setInt(5, sourceNpi);//p_SOURCE_ADDR_NPI
            cs.setString(6, destAddr);//p_DEST_ADDR
            cs.setInt(7, destTon);//p_DEST_ADDR_TON
            cs.setInt(8, destNpi);//p_DEST_ADDR_NPI
            cs.setInt(9, esmClass);//p_ESM_CLASS
            cs.setInt(10, dataCoding);//p_DATA_CODING
            cs.setString(11, shortMessage);//p_SHORT_MESSAGE
            cs.setInt(12, commandStatus);//p_RECEIVE_STATUS
            cs.setInt(13, deduplication ? 1 : 0);//deduplication

            cs.registerOutParameter(14, Types.INTEGER);
            cs.registerOutParameter(15, Types.INTEGER);
            cs.execute();
            int msg_id = cs.getInt(14);
            int msg_status = cs.getInt(15);
            result.setMessage_id(msg_id);
            result.setCommand_status(msg_status);
        } catch (SQLException e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ignored) {
                }
            }
            logger.error("Failed InsertSubmitSM: " + e.toString());
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException ignored) {
            }
        }
        return result;
    }

    public SubmitSM getQueueSubmitSM(String gatewayId) throws SQLException {
        SubmitSM sm = new SubmitSM();
        CallableStatement cs = null;
        try {
            cs = cn.prepareCall("{call  DEQUEUE_SUBMIT_SM(?,?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setString(1, queueName);
            cs.setString(2, gatewayId);
            cs.registerOutParameter(3, Types.INTEGER);//MSG_ID
            cs.registerOutParameter(4, Types.VARCHAR);//sourceAddr
            cs.registerOutParameter(5, Types.SMALLINT);//sourceTon
            cs.registerOutParameter(6, Types.SMALLINT);//sourceNpi
            cs.registerOutParameter(7, Types.VARCHAR); //destAddr
            cs.registerOutParameter(8, Types.SMALLINT);//destTon
            cs.registerOutParameter(9, Types.SMALLINT);//destNpi
            cs.registerOutParameter(10, Types.SMALLINT);//esmClass
            cs.registerOutParameter(11, Types.SMALLINT);//dataCoding
            cs.registerOutParameter(12, Types.VARCHAR);//shortMessage

            cs.execute();
            if (cs.getInt(3) != 0) {
                sm.setSequenceNumber(cs.getInt(3));
                sm.setSourceAddr(cs.getByte(5), cs.getByte(6), cs.getString(4));
                sm.setDestAddr(cs.getByte(8), cs.getByte(9), cs.getString(7));
                sm.setEsmClass(cs.getByte(10));
                sm.setDataCoding(cs.getByte(11));
                sm.setShortMessage(cs.getString(12));
            } else sm = null;
        } catch (SQLException e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ignored) {
                }
            }
            logger.error("Failed getQueueSubmitSM: " + e.getMessage());
            sm = null;
        } catch (WrongLengthOfStringException e) {
            logger.error("Failed getQueueSubmitSM: " + e.getMessage());
            sm = null;
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException ignored) {
            }
        }
        return sm;
    }

    public TypeEnqueueSm enqueueSubmitSM(SubmitSM sm, String username) {
        CallableStatement cs = null;
        TypeEnqueueSm status_sm = new TypeEnqueueSm();
        try {
            cs = cn.prepareCall("{call  SMSQUEUE.ENQUEUE_SUBMIT_SM (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setString(1, queueName);
            cs.setString(2, username);//Username
            cs.setInt(3, 0);//DEDUPLICATION
            cs.setInt(4, sm.getSequenceNumber());//p_SEQ_REQUEST
            cs.setString(5, sm.getSourceAddr().getAddress());//p_SOURCE_ADDR
            cs.setInt(6, sm.getSourceAddr().getTon());//p_SOURCE_ADDR_TON
            cs.setInt(7, sm.getSourceAddr().getNpi());//p_SOURCE_ADDR_NPI
            cs.setString(8, sm.getDestAddr().getAddress());//p_DEST_ADDR
            cs.setInt(9, sm.getDestAddr().getTon());//p_DEST_ADDR_TON
            cs.setInt(10, sm.getDestAddr().getNpi());//p_DEST_ADDR_NPI
            cs.setInt(11, sm.getEsmClass());//p_ESM_CLASS
            cs.setInt(12, sm.getDataCoding());//p_DATA_CODING
            cs.setString(13, sm.getShortMessage());//p_SHORT_MESSAGE
            cs.registerOutParameter(14, Types.INTEGER); //p_ID
            cs.registerOutParameter(15, Types.INTEGER); //p_STATUS
            cs.execute();
            status_sm.setMsg_id(cs.getInt(14));
            status_sm.setCmd_status(cs.getInt(15));
        } catch (SQLException e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ignored) {
                }
            }
            logger.error("Failed enqueueSubmitSM: " + e.toString(), e);
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException ignored) {
            }
        }
        return status_sm;
    }

    public void updateSubmitResp(int sequenceNumber, String messageId, int commandStatus, String gatewayId) {
        CallableStatement cs = null;
        try {
            cs = cn.prepareCall("{call  UPDATE_SUBMIT_RESP(?,?,?,?,?)}");
            cs.setInt(1, sequenceNumber);
            cs.setString(2, messageId);
            cs.setInt(3, commandStatus);
            cs.setString(4, serverID);
            cs.setString(5, gatewayId);
            cs.execute();
        } catch (SQLException e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ignored) {
                }
            }
            logger.error("Failed updateSubmitResp: " + e.toString());
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void updateEnquireLinkResp(String gatewayId, int sequenceNumber) {
        CallableStatement cs = null;
        try {
            cs = cn.prepareCall("{call  UPDATE_ENQUIRELINK_RESP(?,?,?)}");
            cs.setString(1, gatewayId);
            cs.setInt(2, sequenceNumber);
            cs.setString(3, serverID);
            cs.execute();
        } catch (SQLException e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ignored) {
                }
            }
            logger.error("Failed updateEnquireLinkResp: " + e.getMessage(), e);
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException ignored) {
            }
        }
    }


    public void insertMessageCounterPerSecond(int couter, String gatewayId, long time)
            throws SQLException {
        CallableStatement cs = null;
        try {
            cs = cn.prepareCall("{Call INSERT_MESSAGE_COUNTER(?, ?, ?, ?)}");
            cs.setString(1, gatewayId);
            cs.setInt(2, couter);
            cs.setLong(3, time);
            cs.setString(4, serverID);
            cs.execute();
        } catch (Exception e) {
            logger.error("Failed to INSERT_MESSAGE_COUNTER: " + e.getMessage(), e);
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException ignored) {
            }
        }

    }

    public void insertQueueMO(String gateway_id, String source_addr, String dest_addr, String short_message, long receive_seq) throws SQLException {
        String command_code = "";
        CallableStatement cs = null;
        Pattern pDlr = Pattern.compile("(\\.*)\\s(\\.*)");
        Matcher mDlr = pDlr.matcher(short_message);
        if (mDlr.find()) {
            command_code = mDlr.group(1);
        }
        try {
            cs = cn.prepareCall("{Call INSERT_DELIVER_SM(?, ?, ?,?,?,?,?)}");
            cs.setString(1, gateway_id);
            cs.setString(2, source_addr);
            cs.setString(3, dest_addr);
            cs.setString(4, short_message);
            cs.setString(5, command_code);
            cs.setLong(6, receive_seq);
            cs.setString(7, serverID);
            cs.execute();

        } catch (SQLException e) {
            logger.error("Failed insertQueueMO: " + e.toString(), e);
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void updateDeliveryReport(String msg_id, String deliver_status, int error_code) throws SQLException {
        CallableStatement cs = null;
        try {
            cs = cn.prepareCall("{Call UPDATE_DELIVER_REPORT(?, ?, ?)}");
            cs.setString(1, msg_id);
            cs.setString(2, deliver_status);
            cs.setInt(3, error_code);
            cs.execute();
        } catch (SQLException e) {
            logger.error("Failed updateDeliveryReport: " + e.toString(), e);
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void updateDeliverMessage(int mpSeqNo, String gatewayId, String originator, String text, int ordinal, Date date) throws SQLException {
        this.insertQueueMO(gatewayId, originator, "", text, mpSeqNo);
    }

    public void updateDeliverMessage(DeliverSM dm)
            throws SQLException {
        String msg_id = "";
        String msg_status = "";
        int error_code = 0;
        if (dm.getEsmClass() == 4) {
            Pattern pDlr = Pattern.compile("id:(\\d*)\\ssub:(\\d*)\\sdlvrd:(\\d*)\\ssubmit\\sdate:(\\d*)\\sdone\\sdate:(\\d*)\\sstat:(\\w*)\\serr:(\\d*)");
            Matcher mDlr = pDlr.matcher("");
            mDlr.reset(dm.getShortMessage());
            if (mDlr.find()) {
                msg_id = mDlr.group(1);
                msg_status = mDlr.group(6);
                error_code = Integer.valueOf(mDlr.group(7));
            }
            this.updateDeliveryReport(msg_id, msg_status, error_code);
        } else {
            try {
                this.insertQueueMO(dm.getReceiptedMessageId(), dm.getSourceAddr().getAddress(), dm.getDestAddr().getAddress(), dm.getShortMessage(), dm.getSequenceNumber());
            } catch (ValueNotSetException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean authenticated(String username, String password, String sessionId, String ip_address, int commandId) {
        CallableStatement cs = null;
        int authenticated = 0;
        try {
            cs = cn.prepareCall("{call ?:= SMSQUEUE.USER_AUTHENTICATED(?,?,?,?,?)}");
            cs.setString(2, username);
            cs.setString(3, password);
            cs.setString(4, sessionId);
            cs.setString(5, ip_address);
            cs.setInt(6, commandId);
            cs.registerOutParameter(1, Types.INTEGER);
            cs.execute();
            authenticated = cs.getInt(1);
        } catch (Exception e) {
            logger.error("Failed to USER_AUTHENTICATED " + username + " from database: " + e.toString());
        } finally {
            if (cs != null) {
                try {
                    cs.close();
                } catch (SQLException sqle) {
                }
            }
        }
        return (authenticated == 1 ? true : false);
    }

    public void set_user_logout(String sessionId) {
        CallableStatement cs = null;
        try {
            cs = cn.prepareCall("{Call SMSQUEUE.USER_LOGOUT(?)}");
            cs.setString(1, sessionId);
            cs.execute();
        } catch (Exception e) {
            logger.error("Failed to USER_LOGOUT " + sessionId + " from database: " + e.toString());
        } finally {
            if (cs != null) {
                try {
                    cs.close();
                } catch (SQLException sqle) {
                }
            }
        }
    }
}
