#!/bin/sh

FINDNAME=$0
while [ -h $FINDNAME ] ; do FINDNAME=`ls -ld $FINDNAME | awk '{print $NF}'` ; done
SERVER_HOME=`echo $FINDNAME | sed -e 's@/[^/]*$@@'`
unset FINDNAME

if [ "$SERVER_HOME" = '.' ]; then
   SERVER_HOME=$(echo `pwd` | sed 's/\/bin//')
else
   SERVER_HOME=$(echo $SERVER_HOME | sed 's/\/bin//')
fi

HEAP_MEMORY=1024m
PERM_MEMORY=64m
JMX_PORT=9501
SERVER_NAME=stcdemo-app

PIDFILE=$SERVER_HOME/$SERVER_NAME.pid

case $1 in
start)
    echo  "Starting ${SERVER_NAME} ... "

    JAVA_OPTS="-server -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider -XX:+HeapDumpOnOutOfMemoryError -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
    
    shift
    ARGS=($*)
    for ((i=0; i<${#ARGS[@]}; i++)); do
        case "${ARGS[$i]}" in
        -D*)    JAVA_OPTS="${JAVA_OPTS} ${ARGS[$i]}" ;;
        -Heap*) HEAP_MEMORY="${ARGS[$i+1]}" ;;
        -Perm*) PERM_MEMORY="${ARGS[$i+1]}" ;;
        -JmxPort*)  JMX_PORT="${ARGS[$i+1]}" ;;
        esac
    done
    JAVA_OPTS="${JAVA_OPTS} -Xms$HEAP_MEMORY -Xmx$HEAP_MEMORY -XX:PermSize=$PERM_MEMORY -XX:MaxPermSize=$PERM_MEMORY -Dcom.sun.management.jmxremote.port=$JMX_PORT -Duser.dir=$SERVER_HOME -Dapp.name=$SERVER_NAME -Djava.library.path=$SERVER_HOME/lib"
    echo "start jvm args ${JAVA_OPTS}"
    #nohup java $JAVA_OPTS -jar ${SERVER_HOME}/bin/${SERVER_NAME}.jar  >/dev/null &
    nohup java $JAVA_OPTS -jar ${SERVER_HOME}/bin/${SERVER_NAME}.jar  &
    echo $! > $PIDFILE
    echo STARTED
    ;;

stop)
    echo "Stopping ${SERVER_NAME} ... "
    if [ ! -f $PIDFILE ]
    then
        echo "error: count not find file $PIDFILE"
        exit 1
    else
        kill -9 $(cat $PIDFILE)
        rm $PIDFILE
        echo STOPPED
    fi
    ;;

restart)
    ./runctrl.sh stop
    ./runctrl.sh start
    ;;

debug)
    echo  "Starting ${SERVER_NAME} in debug model ... "

    HEAP_MEMORY=1024m
    PERM_MEMORY=64m
    JMX_PORT=9001
    JAVA_OPTS="-server -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider -XX:+HeapDumpOnOutOfMemoryError -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5001"

    shift
    ARGS=($*)
    for ((i=0; i<${#ARGS[@]}; i++)); do
        case "${ARGS[$i]}" in
        -D*)    JAVA_OPTS="${JAVA_OPTS} ${ARGS[$i]}" ;;
        -Heap*) HEAP_MEMORY="${ARGS[$i+1]}" ;;
        -Perm*) PERM_MEMORY="${ARGS[$i+1]}" ;;
        -JmxPort*)  JMX_PORT="${ARGS[$i+1]}" ;;
        esac
    done
    JAVA_OPTS="${JAVA_OPTS} -Xms$HEAP_MEMORY -Xmx$HEAP_MEMORY -XX:PermSize=$PERM_MEMORY -XX:MaxPermSize=$PERM_MEMORY -Dcom.sun.management.jmxremote.port=$JMX_PORT -Duser.home=$SERVER_HOME -Dapp.name=$SERVER_NAME"
    echo "start jvm args ${JAVA_OPTS}"
    nohup java $JAVA_OPTS -jar ${SERVER_NAME}.jar &
    echo $! > $PIDFILE
    echo STARTED
    ;;

*)
    ARGS=($*)
	CMD_NAME=${ARGS[0]}
    shift
    ARGS=($*)
    for ((i=0; i<${#ARGS[@]}; i++)); do
        case "${ARGS[$i]}" in
        -D*)    JAVA_OPTS="${JAVA_OPTS} ${ARGS[$i]}" ;;
        -JmxPort*)  JMX_PORT="${ARGS[$i+1]}" ;;
        esac
    done
    
    java -Duser.dir=$SERVER_HOME -Dapp.name=$SERVER_NAME -jar ${SERVER_HOME}/bin/${SERVER_NAME}.jar -jmxurl service:jmx:rmi:///jndi/rmi://127.0.0.1:${JMX_PORT}/jmxrmi -cmd $CMD_NAME
    ;;
    
esac

exit 0
