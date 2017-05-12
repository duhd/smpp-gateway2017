--
-- Create Schema Script 
--   Database Version          : 11.2.0.3.0 
--   Database Compatible Level : 11.2.0.3.0 
--   Script Compatible Level   : 11.2.0.3.0 
--   Toad Version              : 12.0.0.61 
--   DB Connect String         : 10.22.7.24:1521/VNPTEST 
--   Schema                    : SMSQUEUE 
--   Script Created by         : SMSQUEUE 
--   Script Created at         : 11/24/2014 11:36:46 AM 
--   Physical Location         :  
--   Notes                     :  
--

-- Object Counts: 
--   Functions: 1       Lines of Code: 27 
--   Indexes: 4         Columns: 5          
--   Procedures: 9      Lines of Code: 465 
--   Queues: 2 
--   Queue Tables: 2 
--   Sequences: 2 
--   Tables: 7          Columns: 110        Constraints: 9      
--   Types: 2 
--   Views: 2           Columns: 66         


-- "Set define off" turns off substitution variables. 
Set define off; 

--
-- OBJ_DELIVER_SM  (Type) 
--
CREATE OR REPLACE TYPE SMSQUEUE.obj_DELIVER_SM AS OBJECT
(
   ID INTEGER,
   GATEWAY_ID VARCHAR2 (100 BYTE),
   SOURCE_ADDR VARCHAR2 (16 BYTE),
   DEST_ADDR VARCHAR2 (16 BYTE),
   SHORT_MESSAGE VARCHAR2 (500 BYTE),
   COMMAND_CODE VARCHAR2 (500 BYTE),
   RECEIVE_TIME DATE,
   RECEIVE_SEQ INTEGER,
   TELCO VARCHAR2 (50 BYTE)
)
/

--
-- OBJ_SUBMIT_SM  (Type) 
--
CREATE OR REPLACE TYPE SMSQUEUE.obj_SUBMIT_SM AS OBJECT
 (
   MSG_ID               INTEGER,
   SOURCE_ADDR               VARCHAR2 (16),
   SOURCE_ADDR_TON           SMALLINT,
   SOURCE_ADDR_NPI           SMALLINT,
   DEST_ADDR                 VARCHAR2 (16),
   DEST_ADDR_TON             SMALLINT,
   DEST_ADDR_NPI             SMALLINT,
   ESM_CLASS                 SMALLINT,
   DATA_CODING               SMALLINT,
   SHORT_MESSAGE             VARCHAR2 (500)
)
/

--
-- TBL_TELCO  (Table) 
--
CREATE TABLE SMSQUEUE.TBL_TELCO
(
  TELCOID        INTEGER                        NOT NULL,
  TELCO_NAME     VARCHAR2(50 BYTE),
  PREFIX_NUMBER  VARCHAR2(16 BYTE),
  LEN_NUMBER     INTEGER
)
TABLESPACE TS_SMSQUEUE
RESULT_CACHE (MODE DEFAULT)
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           )
NOCOMPRESS ;


--
-- TBL_QUEUE_SUBMIT_SM  (Table) 
--
BEGIN
  SYS.DBMS_AQADM.CREATE_QUEUE_TABLE
  (
    QUEUE_TABLE           =>  'SMSQUEUE.TBL_QUEUE_SUBMIT_SM'
   ,QUEUE_PAYLOAD_TYPE    =>  'SMSQUEUE.OBJ_SUBMIT_SM'
   ,COMPATIBLE            =>  '10.0.0'
   ,STORAGE_CLAUSE        =>  'NOCOMPRESS
                               TABLESPACE TS_SMSQUEUE
                               RESULT_CACHE (MODE DEFAULT)
                               PCTUSED    0
                               PCTFREE    10
                               INITRANS   1
                               MAXTRANS   255
                               STORAGE    (
                                           INITIAL          64K
                                           NEXT             1M
                                           MAXSIZE          UNLIMITED
                                           MINEXTENTS       1
                                           MAXEXTENTS       UNLIMITED
                                           PCTINCREASE      0
                                           BUFFER_POOL      DEFAULT
                                           FLASH_CACHE      DEFAULT
                                           CELL_FLASH_CACHE DEFAULT
                                          )'
   ,SORT_LIST             =>  'PRIORITY,ENQ_TIME'
   ,MULTIPLE_CONSUMERS    =>  FALSE
   ,MESSAGE_GROUPING      =>  0
   ,SECURE                =>  FALSE
  );
End;
/


--
-- DELIVER_SM  (Table) 
--
CREATE TABLE SMSQUEUE.DELIVER_SM
(
  ID             INTEGER                        NOT NULL,
  GATEWAY_ID     VARCHAR2(100 BYTE),
  SOURCE_ADDR    VARCHAR2(16 BYTE),
  DEST_ADDR      VARCHAR2(16 BYTE),
  SHORT_MESSAGE  VARCHAR2(500 BYTE),
  COMMAND_CODE   VARCHAR2(500 BYTE),
  RECEIVE_TIME   DATE,
  RECEIVE_SEQ    INTEGER,
  TELCO          VARCHAR2(50 BYTE),
  SERVER_ID      VARCHAR2(100 BYTE)
)
TABLESPACE TS_SMSQUEUE
RESULT_CACHE (MODE DEFAULT)
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           )
NOCOMPRESS ;


--
-- TBL_QUEUE_DELIVER_SM  (Table) 
--
BEGIN
  SYS.DBMS_AQADM.CREATE_QUEUE_TABLE
  (
    QUEUE_TABLE           =>  'SMSQUEUE.TBL_QUEUE_DELIVER_SM'
   ,QUEUE_PAYLOAD_TYPE    =>  'SMSQUEUE.OBJ_DELIVER_SM'
   ,COMPATIBLE            =>  '10.0.0'
   ,STORAGE_CLAUSE        =>  'NOCOMPRESS
                               TABLESPACE TS_SMSQUEUE
                               RESULT_CACHE (MODE DEFAULT)
                               PCTUSED    0
                               PCTFREE    10
                               INITRANS   1
                               MAXTRANS   255
                               STORAGE    (
                                           INITIAL          64K
                                           NEXT             1M
                                           MAXSIZE          UNLIMITED
                                           MINEXTENTS       1
                                           MAXEXTENTS       UNLIMITED
                                           PCTINCREASE      0
                                           BUFFER_POOL      DEFAULT
                                           FLASH_CACHE      DEFAULT
                                           CELL_FLASH_CACHE DEFAULT
                                          )'
   ,SORT_LIST             =>  'ENQ_TIME'
   ,MULTIPLE_CONSUMERS    =>  FALSE
   ,MESSAGE_GROUPING      =>  0
   ,SECURE                =>  FALSE
  );
End;
/


--
-- TRAFFIC_LOG  (Table) 
--
CREATE TABLE SMSQUEUE.TRAFFIC_LOG
(
  GATEWAY_ID  VARCHAR2(50 BYTE),
  TRAFFIC     INTEGER,
  DATE_LOG    DATE                              NOT NULL,
  SERVER_ID   VARCHAR2(100 BYTE),
  TIMEMILLIS  NUMBER
)
TABLESPACE TS_SMSQUEUE
RESULT_CACHE (MODE DEFAULT)
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           )
NOCOMPRESS ;


--
-- GATEWAY_LINK_STATUS  (Table) 
--
CREATE TABLE SMSQUEUE.GATEWAY_LINK_STATUS
(
  GATEWAY_ID      VARCHAR2(50 BYTE)             NOT NULL,
  SEQ_LINK        INTEGER,
  TIME_LINK       DATE,
  SERVER_ID       VARCHAR2(100 BYTE),
  MONITOR_ENABLE  CHAR(1 CHAR)                  DEFAULT 'T'
)
TABLESPACE TS_SMSQUEUE
RESULT_CACHE (MODE DEFAULT)
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           )
NOCOMPRESS ;


--
-- SUBMIT_SM  (Table) 
--
CREATE TABLE SMSQUEUE.SUBMIT_SM
(
  ID                  NUMBER                    NOT NULL,
  GATEWAY_ID          VARCHAR2(100 BYTE),
  SEQ_REQUEST         INTEGER,
  SOURCE_ADDR         VARCHAR2(16 BYTE),
  SOURCE_ADDR_TON     INTEGER,
  SOURCE_ADDR_NPI     INTEGER,
  DEST_ADDR           VARCHAR2(16 BYTE),
  DEST_ADDR_TON       INTEGER,
  DEST_ADDR_NPI       INTEGER,
  SERVICE_TYPE        NVARCHAR2(100),
  ESM_CLASS           INTEGER,
  DATA_CODING         INTEGER,
  SHORT_MESSAGE       VARCHAR2(500 BYTE),
  SAR_MSG_REF_NUM     INTEGER,
  SAR_TOTAL_SEGMENTS  INTEGER,
  SAR_SEGMENT_SEQNUM  INTEGER,
  RECEIVE_TIME        DATE,
  SUBMIT_TIME         DATE,
  SUBMIT_STATUS       INTEGER,
  TELCO_ID            VARCHAR2(50 BYTE),
  SM_LENGHT           INTEGER,
  MESSAGE_ID          VARCHAR2(200 BYTE),
  ERROR_CODE          INTEGER,
  DONE_TIME           DATE,
  DELIVERY_STATUS     VARCHAR2(100 BYTE),
  RECEIVE_STATUS      INTEGER,
  HASH_MESSAGE        RAW(32),
  SERVER_ID           VARCHAR2(100 BYTE)
)
TABLESPACE TS_SMSQUEUE
RESULT_CACHE (MODE DEFAULT)
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           )
NOCOMPRESS ;


--
-- SEQ_DELIVER_SM  (Sequence) 
--
CREATE SEQUENCE SMSQUEUE.SEQ_DELIVER_SM
  START WITH 0
  MAXVALUE 9999999999999999999999999999
  MINVALUE 0
  NOCYCLE
  CACHE 20
  NOORDER;


--
-- SEQ_SUBMIT_SM  (Sequence) 
--
CREATE SEQUENCE SMSQUEUE.SEQ_SUBMIT_SM
  START WITH 634440
  MAXVALUE 9999999999999999999999999999
  MINVALUE 0
  NOCYCLE
  CACHE 20
  NOORDER;


--
-- QUEUE_DELIVER_SM  (Queue) 
--
BEGIN
  SYS.DBMS_AQADM.CREATE_QUEUE
  (
    QUEUE_NAME          =>   'SMSQUEUE.QUEUE_DELIVER_SM'
   ,QUEUE_TABLE         =>   'SMSQUEUE.TBL_QUEUE_DELIVER_SM'
   ,QUEUE_TYPE          =>   SYS.DBMS_AQADM.NORMAL_QUEUE
   ,MAX_RETRIES         =>   0
   ,RETRY_DELAY         =>   0
   ,RETENTION_TIME      =>   -1
   );
END;
/

BEGIN
  SYS.DBMS_AQADM.START_QUEUE
  (
    QUEUE_NAME => 'SMSQUEUE.QUEUE_DELIVER_SM'
   ,ENQUEUE => TRUE 
   ,DEQUEUE => TRUE 
   );
END;
/


--
-- QUEUE_SUBMIT_SM  (Queue) 
--
BEGIN
  SYS.DBMS_AQADM.CREATE_QUEUE
  (
    QUEUE_NAME          =>   'SMSQUEUE.QUEUE_SUBMIT_SM'
   ,QUEUE_TABLE         =>   'SMSQUEUE.TBL_QUEUE_SUBMIT_SM'
   ,QUEUE_TYPE          =>   SYS.DBMS_AQADM.NORMAL_QUEUE
   ,MAX_RETRIES         =>   0
   ,RETRY_DELAY         =>   0
   ,RETENTION_TIME      =>   -1
   );
END;
/

BEGIN
  SYS.DBMS_AQADM.START_QUEUE
  (
    QUEUE_NAME => 'SMSQUEUE.QUEUE_SUBMIT_SM'
   ,ENQUEUE => TRUE 
   ,DEQUEUE => TRUE 
   );
END;
/


--
-- GATEWAY_LINK_STATUS_PK  (Index) 
--
CREATE UNIQUE INDEX SMSQUEUE.GATEWAY_LINK_STATUS_PK ON SMSQUEUE.GATEWAY_LINK_STATUS
(GATEWAY_ID)
TABLESPACE TS_SMSQUEUE
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           );


--
-- TRAFFIC_LOG_PK  (Index) 
--
CREATE UNIQUE INDEX SMSQUEUE.TRAFFIC_LOG_PK ON SMSQUEUE.TRAFFIC_LOG
(DATE_LOG, GATEWAY_ID)
TABLESPACE TS_SMSQUEUE
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           );


--
-- SUBMIT_SM_PK  (Index) 
--
CREATE UNIQUE INDEX SMSQUEUE.SUBMIT_SM_PK ON SMSQUEUE.SUBMIT_SM
(ID)
TABLESPACE TS_SMSQUEUE
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           );


--
-- DELIVER_SM_PK  (Index) 
--
CREATE UNIQUE INDEX SMSQUEUE.DELIVER_SM_PK ON SMSQUEUE.DELIVER_SM
(ID)
TABLESPACE TS_SMSQUEUE
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
            FLASH_CACHE      DEFAULT
            CELL_FLASH_CACHE DEFAULT
           );


--
-- DEQUEUE_SUBMIT_SM  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.DEQUEUE_SUBMIT_SM (
   p_QUEUE_NAME        IN     VARCHAR2,
   p_GATEWAY_ID        IN     VARCHAR2,
   p_MSG_ID               OUT INTEGER,
   p_SOURCE_ADDR          OUT VARCHAR2,
   p_SOURCE_ADDR_TON      OUT SMALLINT,
   p_SOURCE_ADDR_NPI      OUT SMALLINT,
   p_DEST_ADDR            OUT VARCHAR2,
   p_DEST_ADDR_TON        OUT SMALLINT,
   p_DEST_ADDR_NPI        OUT SMALLINT,
   p_ESM_CLASS            OUT SMALLINT,
   p_DATA_CODING          OUT SMALLINT,
   p_SHORT_MESSAGE        OUT VARCHAR2)
 
IS
   queue_options        DBMS_AQ.DEQUEUE_OPTIONS_T;
   message_properties   DBMS_AQ.MESSAGE_PROPERTIES_T;
   message_id           RAW (16);
   obj_sm               SMSQUEUE.OBJ_SUBMIT_SM;
BEGIN
   queue_options.dequeue_mode := DBMS_AQ.REMOVE;
   queue_options.navigation := DBMS_AQ.FIRST_MESSAGE;
   queue_options.visibility := DBMS_AQ.IMMEDIATE;
   queue_options.wait := 10;


   DBMS_AQ.DEQUEUE (queue_name           => p_QUEUE_NAME,
                    dequeue_options      => queue_options,
                    message_properties   => message_properties,
                    payload              => obj_sm,
                    msgid                => message_id);

   IF (obj_sm IS NOT NULL)
   THEN
      p_MSG_ID := obj_sm.MSG_ID;
      p_SOURCE_ADDR := obj_sm.SOURCE_ADDR;
      p_SOURCE_ADDR_TON := obj_sm.SOURCE_ADDR_TON;
      p_SOURCE_ADDR_NPI := obj_sm.SOURCE_ADDR_NPI;
      p_DEST_ADDR := obj_sm.DEST_ADDR;
      p_DEST_ADDR_TON := obj_sm.DEST_ADDR_TON;
      p_DEST_ADDR_NPI := obj_sm.DEST_ADDR_NPI;
      p_ESM_CLASS := obj_sm.ESM_CLASS;
      p_DATA_CODING := obj_sm.DATA_CODING;
      p_SHORT_MESSAGE := obj_sm.SHORT_MESSAGE;
   ELSE
      p_MSG_ID := 0;
   END IF;
   --UPDATE SUBMIT_SM SET GATEWAY_ID=p_GATEWAY_ID WHERE ID = p_MSG_ID;
   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
     p_MSG_ID := 0;

END DEQUEUE_SUBMIT_SM;
/

--
-- UPDATE_ENQUIRELINK_RESP  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.UPDATE_ENQUIRELINK_RESP (
   gatewayId        IN VARCHAR2,
   sequenceNumber   IN INTEGER,
   serverId         IN VARCHAR2)
IS
BEGIN
   UPDATE GATEWAY_LINK_STATUS
      SET SEQ_LINK = sequenceNumber,
          TIME_LINK = SYSDATE,
          SERVER_ID = serverId
    WHERE GATEWAY_ID = gatewayId;



   IF (SQL%NOTFOUND)
   THEN
      INSERT INTO GATEWAY_LINK_STATUS (GATEWAY_ID,
                                       TIME_LINK,
                                       SEQ_LINK,
                                       SERVER_ID)
           VALUES (gatewayId,
                   SYSDATE,
                   sequenceNumber,
                   serverId);
   END IF;

   COMMIT;
END UPDATE_ENQUIRELINK_RESP;
/

--
-- INSERT_MESSAGE_COUNTER  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.INSERT_MESSAGE_COUNTER (
   gatewayId        IN VARCHAR2,
   traffic   IN INTEGER,
   TimeMillis IN NUMBER,
   serverId         IN VARCHAR2)
IS
BEGIN
   
      INSERT INTO TRAFFIC_LOG (GATEWAY_ID,
                                       TRAFFIC,
                                       DATE_LOG,TimeMillis,
                                       SERVER_ID)
           VALUES (gatewayId,
                    traffic,
                   SYSDATE,
                   TimeMillis,
                   serverId);
   COMMIT;
END INSERT_MESSAGE_COUNTER;
/

--
-- UPDATE_SUBMIT_RESP  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.UPDATE_SUBMIT_RESP (
   sequenceNumber   IN INTEGER,
   messageId        IN VARCHAR2,
   commandStatus    IN INTEGER,
   serverId         IN VARCHAR2,
   gatewayId        IN VARCHAR2)
IS
BEGIN
   UPDATE SUBMIT_SM
      SET MESSAGE_ID = messageId,
          SUBMIT_STATUS = commandStatus,
          SUBMIT_TIME = SYSDATE,
          SERVER_ID = serverId,
          GATEWAY_ID = gatewayId
    WHERE ID = sequenceNumber;

   COMMIT;
EXCEPTION
   WHEN NO_DATA_FOUND
   THEN
      NULL;
   WHEN OTHERS
   THEN
      RAISE;
END UPDATE_SUBMIT_RESP;
/

--
-- UPDATE_DELIVER_REPORT  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.UPDATE_DELIVER_REPORT (
   messageId   IN VARCHAR2,
   deliver_status    IN VARCHAR2,
   error_code IN INTEGER)
IS
BEGIN
   UPDATE SUBMIT_SM
      SET DELIVERY_STATUS = deliver_status, ERROR_CODE = error_code, DONE_TIME = SYSDATE
    WHERE MESSAGE_ID = messageId;

   COMMIT;
EXCEPTION
   WHEN NO_DATA_FOUND
   THEN
      NULL;
   WHEN OTHERS
   THEN
      RAISE;
END UPDATE_DELIVER_REPORT;
/

--
-- DEQUEUE_DELIVER_SM  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.DEQUEUE_DELIVER_SM (
   p_MO_ID           OUT INTEGER,
   p_GATEWAY_ID      OUT VARCHAR2,
   p_SOURCE_ADDR     OUT VARCHAR2,
   p_DEST_ADDR       OUT VARCHAR2,
   p_SHORT_MESSAGE   OUT VARCHAR2,
   p_COMMAND_CODE    OUT VARCHAR2,
   p_RECEIVE_TIME    OUT DATE,
   p_RECEIVE_SEQ     OUT INTEGER,
   p_TELCO           OUT VARCHAR2)
IS
   queue_options        DBMS_AQ.DEQUEUE_OPTIONS_T;
   message_properties   DBMS_AQ.MESSAGE_PROPERTIES_T;
   message_id           RAW (16);
   obj_dm               SMSQUEUE.OBJ_DELIVER_SM;
BEGIN
   queue_options.dequeue_mode := DBMS_AQ.REMOVE;
   queue_options.navigation := DBMS_AQ.FIRST_MESSAGE;
   queue_options.visibility := DBMS_AQ.IMMEDIATE;
   queue_options.wait := 10;


   DBMS_AQ.DEQUEUE (queue_name           => 'QUEUE_DELIVER_SM',
                    dequeue_options      => queue_options,
                    message_properties   => message_properties,
                    payload              => obj_dm,
                    msgid                => message_id);

   IF (obj_dm IS NOT NULL)
   THEN
      p_MO_ID := obj_dm.ID;
      p_GATEWAY_ID := obj_dm.GATEWAY_ID;
      p_SOURCE_ADDR := obj_dm.SOURCE_ADDR;
      p_DEST_ADDR := obj_dm.DEST_ADDR;
      p_SHORT_MESSAGE := obj_dm.SHORT_MESSAGE;
      p_COMMAND_CODE := obj_dm.COMMAND_CODE;
      p_RECEIVE_TIME := obj_dm.RECEIVE_TIME;
      p_RECEIVE_SEQ := obj_dm.RECEIVE_SEQ;
      p_TELCO := obj_dm.TELCO;
   ELSE
      p_MO_ID := 0;
   END IF;

   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      p_MO_ID := 0;
END DEQUEUE_DELIVER_SM;
/

--
-- GET_TELCO  (Function) 
--
CREATE OR REPLACE FUNCTION SMSQUEUE.GET_TELCO (p_DEST_ADDR IN VARCHAR2)
   RETURN VARCHAR2
IS
   v_TELCO_ID   VARCHAR2 (50) := NULL;
BEGIN
   --Xac dinh TELCO_ID
   SELECT TELCO_NAME
     INTO v_TELCO_ID
     FROM TBL_TELCO
    WHERE     REGEXP_INSTR (p_DEST_ADDR,
                            '^' || "PREFIX_NUMBER",
                            1,
                            1,
                            0,
                            'i') = 1
          AND LEN_NUMBER = LENGTH (p_DEST_ADDR)
          AND ROWNUM < 2;

   RETURN v_TELCO_ID;
EXCEPTION
   WHEN NO_DATA_FOUND
   THEN
      RETURN null;
   WHEN OTHERS
   THEN
      RAISE;
END GET_TELCO;
/

--
-- AQ$_TBL_QUEUE_DELIVER_SM_F  (View) 
--
CREATE OR REPLACE FORCE VIEW SMSQUEUE.AQ$_TBL_QUEUE_DELIVER_SM_F
(Q_NAME, ROW_ID, MSGID, CORRID, PRIORITY, 
 STATE, DELAY, EXPIRATION, ENQ_TIME, ENQ_UID, 
 ENQ_TID, DEQ_TIME, DEQ_UID, DEQ_TID, RETRY_COUNT, 
 EXCEPTION_QSCHEMA, EXCEPTION_QUEUE, CSCN, DSCN, CHAIN_NO, 
 LOCAL_ORDER_NO, TIME_MANAGER_INFO, STEP_NO, USER_DATA, SENDER_NAME, 
 SENDER_ADDRESS, SENDER_PROTOCOL, DEQUEUE_MSGID, DELIVERY_MODE, SEQUENCE_NUM, 
 MSG_NUM, QUEUE_ID, USER_PROP)
AS 
SELECT  /*+ NO_MERGE (qo) USE_NL(qt) */ qt.q_name Q_NAME, qt.rowid ROW_ID, qt.msgid MSGID, qt.corrid CORRID, qt.priority PRIORITY, qt.state STATE, cast(FROM_TZ(qt.delay, '00:00') at time zone sessiontimezone as timestamp) DELAY, qt.expiration EXPIRATION, cast(FROM_TZ(qt.enq_time, '00:00') at time zone sessiontimezone as timestamp) ENQ_TIME, qt.enq_uid ENQ_UID, qt.enq_tid ENQ_TID, cast(FROM_TZ(qt.deq_time, '00:00') at time zone sessiontimezone as timestamp) DEQ_TIME, qt.deq_uid DEQ_UID, qt.deq_tid DEQ_TID, qt.retry_count RETRY_COUNT, qt.exception_qschema EXCEPTION_QSCHEMA, qt.exception_queue EXCEPTION_QUEUE, qt.cscn CSCN, qt.dscn DSCN, qt.chain_no CHAIN_NO, qt.local_order_no LOCAL_ORDER_NO, cast(FROM_TZ(qt.time_manager_info, '00:00') at time zone sessiontimezone as timestamp)   TIME_MANAGER_INFO, qt.step_no STEP_NO, qt.user_data USER_DATA ,qt.sender_name SENDER_NAME, qt.sender_address SENDER_ADDRESS, qt.sender_protocol SENDER_PROTOCOL, qt.dequeue_msgid DEQUEUE_MSGID, 'PERSISTENT' DELIVERY_MODE, 0 SEQUENCE_NUM, 0 MSG_NUM, qo.qid QUEUE_ID, qt.user_prop USER_PROP FROM "TBL_QUEUE_DELIVER_SM" qt, SYS.ALL_INT_DEQUEUE_QUEUES qo  WHERE qt.q_name = qo.name AND qo.owner = 'SMSQUEUE' WITH READ ONLY;


--
-- AQ$_TBL_QUEUE_SUBMIT_SM_F  (View) 
--
CREATE OR REPLACE FORCE VIEW SMSQUEUE.AQ$_TBL_QUEUE_SUBMIT_SM_F
(Q_NAME, ROW_ID, MSGID, CORRID, PRIORITY, 
 STATE, DELAY, EXPIRATION, ENQ_TIME, ENQ_UID, 
 ENQ_TID, DEQ_TIME, DEQ_UID, DEQ_TID, RETRY_COUNT, 
 EXCEPTION_QSCHEMA, EXCEPTION_QUEUE, CSCN, DSCN, CHAIN_NO, 
 LOCAL_ORDER_NO, TIME_MANAGER_INFO, STEP_NO, USER_DATA, SENDER_NAME, 
 SENDER_ADDRESS, SENDER_PROTOCOL, DEQUEUE_MSGID, DELIVERY_MODE, SEQUENCE_NUM, 
 MSG_NUM, QUEUE_ID, USER_PROP)
AS 
SELECT  /*+ NO_MERGE (qo) USE_NL(qt) */ qt.q_name Q_NAME, qt.rowid ROW_ID, qt.msgid MSGID, qt.corrid CORRID, qt.priority PRIORITY, qt.state STATE, cast(FROM_TZ(qt.delay, '00:00') at time zone sessiontimezone as timestamp) DELAY, qt.expiration EXPIRATION, cast(FROM_TZ(qt.enq_time, '00:00') at time zone sessiontimezone as timestamp) ENQ_TIME, qt.enq_uid ENQ_UID, qt.enq_tid ENQ_TID, cast(FROM_TZ(qt.deq_time, '00:00') at time zone sessiontimezone as timestamp) DEQ_TIME, qt.deq_uid DEQ_UID, qt.deq_tid DEQ_TID, qt.retry_count RETRY_COUNT, qt.exception_qschema EXCEPTION_QSCHEMA, qt.exception_queue EXCEPTION_QUEUE, qt.cscn CSCN, qt.dscn DSCN, qt.chain_no CHAIN_NO, qt.local_order_no LOCAL_ORDER_NO, cast(FROM_TZ(qt.time_manager_info, '00:00') at time zone sessiontimezone as timestamp)   TIME_MANAGER_INFO, qt.step_no STEP_NO, qt.user_data USER_DATA ,qt.sender_name SENDER_NAME, qt.sender_address SENDER_ADDRESS, qt.sender_protocol SENDER_PROTOCOL, qt.dequeue_msgid DEQUEUE_MSGID, 'PERSISTENT' DELIVERY_MODE, 0 SEQUENCE_NUM, 0 MSG_NUM, qo.qid QUEUE_ID, qt.user_prop USER_PROP FROM "TBL_QUEUE_SUBMIT_SM" qt, SYS.ALL_INT_DEQUEUE_QUEUES qo  WHERE qt.q_name = qo.name AND qo.owner = 'SMSQUEUE' WITH READ ONLY;


--
-- ENQUEUE_SUBMIT_SM  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.Enqueue_SUBMIT_SM (
   queuename           IN     VARCHAR2,
   priority            IN     INTEGER DEFAULT 1,
   deduplication       IN     INTEGER DEFAULT 0,
   p_SEQ_REQUEST       IN     INTEGER,
   p_SOURCE_ADDR       IN     VARCHAR2,
   p_SOURCE_ADDR_TON   IN     INTEGER,
   p_SOURCE_ADDR_NPI   IN     INTEGER,
   p_DEST_ADDR         IN     VARCHAR2,
   p_DEST_ADDR_TON     IN     INTEGER,
   p_DEST_ADDR_NPI     IN     INTEGER,
   p_ESM_CLASS         IN     INTEGER,
   p_DATA_CODING       IN     INTEGER,
   p_SHORT_MESSAGE     IN     VARCHAR2,
   p_ID                   OUT INTEGER)
IS
   queue_options        DBMS_AQ.ENQUEUE_OPTIONS_T;
   message_properties   DBMS_AQ.MESSAGE_PROPERTIES_T;
   message_id           RAW (16);
   msg                  OBJ_SUBMIT_SM;
   hash_msg             RAW (32);
   v_STATUS             INTEGER := 0;
   v_ID                 NUMBER;
   v_TELCO_ID           VARCHAR2 (50);

   CURSOR c_msg
   IS
      SELECT -8
        FROM SUBMIT_SM
       WHERE     HASH_MESSAGE = hash_msg
             AND RECEIVE_STATUS = 0
             AND SUBMIT_STATUS = 0
             AND ROWNUM < 2;
BEGIN
   queue_options.visibility := DBMS_AQ.IMMEDIATE;
   message_properties.priority := priority;
   message_properties.delay := 0;

   SELECT SEQ_SUBMIT_SM.NEXTVAL INTO v_ID FROM DUAL;

   v_TELCO_ID := SMSQUEUE.GET_TELCO (P_DEST_ADDR);

   hash_msg :=
      UTL_RAW.CAST_TO_RAW (
         DBMS_OBFUSCATION_TOOLKIT.MD5 (
            INPUT_STRING   =>    p_DEST_ADDR
                              || p_SHORT_MESSAGE
                              || TO_CHAR (SYSDATE, 'YYYY/MM/DD')));

   --Kiem tra neu chong trung
   IF (deduplication = 1)
   THEN
      OPEN c_msg;

      FETCH c_msg INTO v_STATUS;

      CLOSE c_msg;
   END IF;

   IF (v_TELCO_ID IS NULL)
   THEN
      v_STATUS := 11;
   END IF;

   IF (v_STATUS = 0 )
   THEN
      msg :=
         OBJ_SUBMIT_SM (v_ID,
                        p_SOURCE_ADDR,
                        p_SOURCE_ADDR_TON,
                        p_SOURCE_ADDR_NPI,
                        p_DEST_ADDR,
                        p_DEST_ADDR_TON,
                        p_DEST_ADDR_NPI,
                        p_ESM_CLASS,
                        p_DATA_CODING,
                        p_SHORT_MESSAGE);

      DBMS_AQ.ENQUEUE (queue_name           => queuename,
                       enqueue_options      => queue_options,
                       message_properties   => message_properties,
                       payload              => msg,
                       msgid                => message_id);
   END IF;

   --Luu SUBMIT_SM

   INSERT INTO SUBMIT_SM (ID,
                          SEQ_REQUEST,
                          SOURCE_ADDR,
                          SOURCE_ADDR_TON,
                          SOURCE_ADDR_NPI,
                          DEST_ADDR,
                          DEST_ADDR_TON,
                          DEST_ADDR_NPI,
                          ESM_CLASS,
                          DATA_CODING,
                          SHORT_MESSAGE,
                          TELCO_ID,
                          HASH_MESSAGE,
                          RECEIVE_TIME,
                          RECEIVE_STATUS)
        VALUES (v_ID,
                p_SEQ_REQUEST,
                p_SOURCE_ADDR,
                p_SOURCE_ADDR_TON,
                p_SOURCE_ADDR_NPI,
                p_DEST_ADDR,
                p_DEST_ADDR_TON,
                p_DEST_ADDR_NPI,
                p_ESM_CLASS,
                p_DATA_CODING,
                p_SHORT_MESSAGE,
                v_TELCO_ID,
                hash_msg,
                SYSDATE,
                v_STATUS);

   p_ID := v_ID;

   COMMIT;
END Enqueue_SUBMIT_SM;
/

--
-- INSERT_DELIVER_SM  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.INSERT_DELIVER_SM (
   p_GATEWAY_ID      IN VARCHAR2,
   p_SEQ_REQUEST     IN INTEGER,
   p_SOURCE_ADDR     IN VARCHAR2,
   p_DEST_ADDR       IN VARCHAR2,
   p_SHORT_MESSAGE   IN VARCHAR2,
   p_COMMAND_CODE    IN VARCHAR2,
   p_RECEIVE_SEQ     IN INTEGER,
   p_SERVER_ID       IN VARCHAR2)
IS
   v_ID         INTEGER;
   v_TELCO_ID   VARCHAR2 (50);
BEGIN
   SELECT SEQ_DELIVER_SM.NEXTVAL INTO v_ID FROM DUAL;

   v_TELCO_ID := SMSQUEUE.GET_TELCO (P_DEST_ADDR);

   INSERT INTO SMSQUEUE.DELIVER_SM (ID,
                                    GATEWAY_ID,
                                    SOURCE_ADDR,
                                    DEST_ADDR,
                                    SHORT_MESSAGE,
                                    COMMAND_CODE,
                                    RECEIVE_TIME,
                                    RECEIVE_SEQ,
                                    TELCO,
                                    SERVER_ID)
        VALUES (v_ID,
                p_GATEWAY_ID,
                p_SOURCE_ADDR,
                p_DEST_ADDR,
                p_SHORT_MESSAGE,
                p_COMMAND_CODE,
                SYSDATE,
                p_RECEIVE_SEQ,
                v_TELCO_ID,
                p_SERVER_ID);

   COMMIT;
EXCEPTION
   WHEN NO_DATA_FOUND
   THEN
      NULL;
   WHEN OTHERS
   THEN
      RAISE;
END INSERT_DELIVER_SM;
/

--
-- INSERT_SUBMIT_SM  (Procedure) 
--
CREATE OR REPLACE PROCEDURE SMSQUEUE.INSERT_SUBMIT_SM (
   p_GATEWAY_ID        IN     VARCHAR2,
   p_SEQ_REQUEST       IN     INTEGER,
   p_SOURCE_ADDR       IN     VARCHAR2,
   p_SOURCE_ADDR_TON   IN     INTEGER,
   p_SOURCE_ADDR_NPI   IN     INTEGER,
   p_DEST_ADDR         IN     VARCHAR2,
   p_DEST_ADDR_TON     IN     INTEGER,
   p_DEST_ADDR_NPI     IN     INTEGER,
   p_ESM_CLASS         IN     INTEGER,
   p_DATA_CODING       IN     INTEGER,
   p_SHORT_MESSAGE     IN     VARCHAR2,
   p_RECEIVE_STATUS    IN     INTEGER,
   deduplication       IN     INTEGER DEFAULT 0,
   p_ID                   OUT INTEGER,
   p_STATUS               OUT INTEGER)
IS
   v_hash_msg   RAW (32);
   v_STATUS     INTEGER := 0;
   v_ID         INTEGER;
   v_TELCO_ID   VARCHAR2 (50);

   CURSOR c_msg
   IS
      SELECT -8
        FROM SUBMIT_SM
       WHERE     HASH_MESSAGE = v_hash_msg
             AND RECEIVE_STATUS = 0
             AND SUBMIT_STATUS = 0
             AND ROWNUM < 2;
BEGIN
   SELECT SEQ_SUBMIT_SM.NEXTVAL INTO v_ID FROM DUAL;

   v_TELCO_ID := SMSQUEUE.GET_TELCO (P_DEST_ADDR);

   p_ID := v_ID;

   v_hash_msg :=
      UTL_RAW.CAST_TO_RAW (
         DBMS_OBFUSCATION_TOOLKIT.MD5 (
            INPUT_STRING   =>    p_DEST_ADDR
                              || p_SHORT_MESSAGE
                              || TO_CHAR (SYSDATE, 'YYYY/MM/DD')));

   --Kiem tra neu chong trung
   IF (deduplication = 1)
   THEN
      OPEN c_msg;

      FETCH c_msg INTO v_STATUS;

      CLOSE c_msg;
   END IF;


   IF (v_TELCO_ID IS NULL)
   THEN
      v_STATUS := 11;
   END IF;

   IF p_RECEIVE_STATUS <> 0
   THEN
      v_STATUS := p_RECEIVE_STATUS;
   END IF;

   INSERT INTO SUBMIT_SM (ID,
                          GATEWAY_ID,
                          SEQ_REQUEST,
                          SOURCE_ADDR,
                          SOURCE_ADDR_TON,
                          SOURCE_ADDR_NPI,
                          DEST_ADDR,
                          DEST_ADDR_TON,
                          DEST_ADDR_NPI,
                          ESM_CLASS,
                          DATA_CODING,
                          SHORT_MESSAGE,
                          TELCO_ID,
                          HASH_MESSAGE,
                          RECEIVE_TIME,
                          RECEIVE_STATUS)
        VALUES (v_ID,
                p_GATEWAY_ID,
                p_SEQ_REQUEST,
                p_SOURCE_ADDR,
                p_SOURCE_ADDR_TON,
                p_SOURCE_ADDR_NPI,
                p_DEST_ADDR,
                p_DEST_ADDR_TON,
                p_DEST_ADDR_NPI,
                p_ESM_CLASS,
                p_DATA_CODING,
                p_SHORT_MESSAGE,
                v_TELCO_ID,
                v_hash_msg,
                SYSDATE,
                v_STATUS);

   COMMIT;
   p_STATUS := v_STATUS;
END INSERT_SUBMIT_SM;
/

-- 
-- Non Foreign Key Constraints for Table DELIVER_SM 
-- 
ALTER TABLE SMSQUEUE.DELIVER_SM ADD (
  CONSTRAINT DELIVER_SM_PK
  PRIMARY KEY
  (ID)
  USING INDEX SMSQUEUE.DELIVER_SM_PK
  ENABLE VALIDATE);


-- 
-- Non Foreign Key Constraints for Table TRAFFIC_LOG 
-- 
ALTER TABLE SMSQUEUE.TRAFFIC_LOG ADD (
  CONSTRAINT TRAFFIC_LOG_PK
  PRIMARY KEY
  (DATE_LOG, GATEWAY_ID)
  USING INDEX SMSQUEUE.TRAFFIC_LOG_PK
  ENABLE VALIDATE);


-- 
-- Non Foreign Key Constraints for Table GATEWAY_LINK_STATUS 
-- 
ALTER TABLE SMSQUEUE.GATEWAY_LINK_STATUS ADD (
  CONSTRAINT GATEWAY_LINK_STATUS_PK
  PRIMARY KEY
  (GATEWAY_ID)
  USING INDEX SMSQUEUE.GATEWAY_LINK_STATUS_PK
  ENABLE VALIDATE);


-- 
-- Non Foreign Key Constraints for Table SUBMIT_SM 
-- 
ALTER TABLE SMSQUEUE.SUBMIT_SM ADD (
  CONSTRAINT SUBMIT_SM_PK
  PRIMARY KEY
  (ID)
  USING INDEX SMSQUEUE.SUBMIT_SM_PK
  ENABLE VALIDATE);