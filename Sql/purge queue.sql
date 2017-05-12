-- purge queue
DECLARE
 po_t dbms_aqadm.aq$_purge_options_t;
BEGIN
  dbms_aqadm.purge_queue_table('TBL_QUEUE_SUBMIT_SM', NULL, po_t);
END;


BEGIN
  DBMS_AQADM.STOP_QUEUE(queue_name => 'MO_VT');
  DBMS_AQADM.DROP_QUEUE(queue_name => 'MO_VT');
  DBMS_AQADM.DROP_QUEUE_TABLE(queue_table => 'QTB_MO_VT');
END;