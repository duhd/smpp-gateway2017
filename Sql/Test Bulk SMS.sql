/* Formatted on 11/14/2014 4:24:21 PM (QP5 v5.252.13127.32867) */
DECLARE
   QUEUENAME           VARCHAR2 (50);
   PRIORITY            INTEGER;
   DEDUPLICATION       INTEGER;
   P_SEQ_REQUEST       NUMBER;
   P_SOURCE_ADDR       VARCHAR2 (16);
   P_SOURCE_ADDR_TON   INTEGER;
   P_SOURCE_ADDR_NPI   INTEGER;
   P_DEST_ADDR         VARCHAR2 (16);
   P_DEST_ADDR_TON     INTEGER;
   P_DEST_ADDR_NPI     INTEGER;
   P_ESM_CLASS         INTEGER;
   P_DATA_CODING       INTEGER;
   P_SHORT_MESSAGE     VARCHAR2 (500);
   P_ID                INTEGER;
BEGIN
   QUEUENAME := 'QUEUE_SUBMIT_SM';
   PRIORITY := 1;
   DEDUPLICATION := 0;
   P_SEQ_REQUEST := 1111;
   P_SOURCE_ADDR := 'VNPAY';
   P_SOURCE_ADDR_TON := 1;
   P_SOURCE_ADDR_NPI := 1;
   P_DEST_ADDR := '84983551065';
   P_DEST_ADDR_TON := 1;
   P_DEST_ADDR_NPI := 1;
   P_ESM_CLASS := 0;
   P_DATA_CODING := 0;
   P_SHORT_MESSAGE := 'Test EnQueue SMS ';

   FOR Lcntr IN 2000 .. 12000
   LOOP
      SMSQUEUE.ENQUEUE_SUBMIT_SM (QUEUENAME,
                                  PRIORITY,
                                  DEDUPLICATION,
                                  Lcntr,                      --P_SEQ_REQUEST,
                                  P_SOURCE_ADDR,
                                  P_SOURCE_ADDR_TON,
                                  P_SOURCE_ADDR_NPI,
                                  P_DEST_ADDR,
                                  P_DEST_ADDR_TON,
                                  P_DEST_ADDR_NPI,
                                  P_ESM_CLASS,
                                  P_DATA_CODING,
                                  P_SHORT_MESSAGE || TO_CHAR(Lcntr, '000099'),
                                  P_ID);

      --Return MSG_ID
      DBMS_OUTPUT.PUT_LINE (P_ID);
   END LOOP;
END;