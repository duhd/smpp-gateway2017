#!/bin/sh
JAVA_HOME=/usr/java/latest
JAVA_OPTS="-Xms256m -Xmx2g"
# Xms represents the initial size of total heap space
# Xmx represents the maximum size of total heap space

PROGRAM_NAME=SMPP-Server
PROGRAM_HOME=/home/smsgateway/smpp-client
PROGRAM_PID=$PROGRAM_HOME/smpp-server.pid
PROGRAM_USER=smsgateway

export JAVA_HOME
export PROGRAM_HOME
export PROGRAM_NAME
export PROGRAM_PID
export JAVA_OPTS

RUN_DIR=/home/smsgateway/smpp-client
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
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/ucp.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/ojdbc8.jar
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$MYAPP_LIBS/log4j-1.2.12.jar



if [ $? = 0 -a -f $RUN_DIR/smpp-client.pid ]
	then
		echo Process already started. Proccess going to stop...
		echo "Stop $PROGRAM_NAME Service!"
		set `cat $PROGRAM_HOME/smpp-server.pid`
		echo "Killing $PROGRAM_NAME Proccess. PID= $1"
		kill $1
		rm $PROGRAM_HOME/smpp-client.pid
	fi
#Start
ulimit -n 256
su - $PROGRAM_USER -c "java ${JAVA_OPTS} -cp $MYAPP_CLASSPATH  vn.vnpay.sms.client.SmsGateway"
exit 0