#!/bin/sh

RUN_DIR=/home/smsgateway/smpp-server
MYAPP_LIBS=$RUN_DIR/libs
MYAPP_CLASSPATH=$RUN_DIR/classes
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$RUN_DIR/conf
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/commons-codec-1.6.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/commons-collections-3.1.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/commons-configuration-1.6.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/commons-discovery-0.2.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/commons-lang3-3.1.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/commons-lang-2.3.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/commons-logging-1.1.1.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/ojdbc8.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/ucp.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/log4j-1.2.12.jar



if [ -f /var/lock/subsys/smpp-server.pid ]
then
    ps -p `cat $RUN_DIR/smpp-server.pid 2>/dev/null | awk "{ print "'$1'" }"` | grep 'java' > /dev/null
fi
if [ $? = 0 -a -f $RUN_DIR/smpp-server.pid ]
then
    echo Process already started. Cannot start twice.
    exit 1
else
    ulimit -n 256
    nohup java ${JAVA_OPTS} -cp $MYAPP_CLASSPATH  vn.vnpay.sms.receiver.SMSCServer 1>/dev/null 2>/dev/null &
    PROCESSID=$!
    echo $PROCESSID > $RUN_DIR/smpp-server.pid
    echo Process started with PID=$PROCESSID
fi
exit 0
