#!/bin/sh
#$VERSION$=20210209111111

### BEGIN INIT INFO
# chkconfig: 345 99 10
# Provides:       hagent
# Required-Start: $remote_fs $named
# Required-Stop:  $remote_fs $named
# Default-Start:  3 5
# Default-Stop:   4
# Description:    Start HAgent
### END INIT INFO




if [ $(uname) == "AIX" ] 
then
	JAVA_BIN=/usr/java8_64/jre/bin/java
else
	JAVA_BIN=/usr/lib64/jvm/jre-1.8.0/bin/java
fi

LOG_DIR=/var/log/hagent
BASE_DIR=/usr/local/hagent

CONFIG_FILE=$BASE_DIR/config.json
LOG4J_CONFIG=$BASE_DIR/log4j2.xml
JAR_FILE=$BASE_DIR/hagent.jar


start() {

        echo "Iniciando el HAgente ..."

        if [ $(is_running) -ne 0 ]
        then
                echo "El HAgente ya esta ejecutandose con el PID $(get_pid)"
                return 0
        fi

        echo "Arrancamos el HAgente ..."
        nohup $JAVA_BIN -XX:OnOutOfMemoryError="$BASE_DIR/apihagent.sh restart" -Xmx64m -Dlog4j.configurationFile=$LOG4J_CONFIG -Dhagent.configurationFile=$CONFIG_FILE -jar $JAR_FILE > $LOG_DIR/critical.log 2> $LOG_DIR/critical.log &
        sleep 6


        if [ $(is_running) -eq 0 ]
        then
                echo "No he podido arrancar el HAgente"
                echo "------------------------------------------------------------------------------------------"
                cat $LOG_DIR/critical.log
                echo "------------------------------------------------------------------------------------------"
                echo "Revisa los logs en $LOG_DIR"
                return 1
        else
                echo "HAgente arrancado"
                return 0
        fi
}


fstop() {

        echo "Parando el HAgente ..."

        if [ $(is_running) -eq 0 ]
        then
                echo "El HAgente ya estaba parado"
                return 0
        fi

        echo "Mandando SIGINT al HAgente ..."
        kill $(get_pid)
        sleep 6

        IDX=1
        while [[ $(is_running) -ne 0 && $IDX -lt 3 ]]
        do
                IDX=$(( $IDX + 1 ))

                if [[ $IDX -lt 3 ]]
                then
                        echo "El HAgente sigue en ejecucion, le lanzamos otro SIGINT"
                        kill $(get_pid)
                else
                        echo "Ya me he cansado de esperar... le lanzamos un SIGQUIT"
                        kill -9 $(get_pid)
                fi

                sleep $(( $IDX * 2 ))
        done



        if [ $(is_running) -ne 0 ]
        then
                echo "No he podido parar el HAgente"
                echo "Revisa los logs en $LOG_DIR"
                return 1
        else
                echo "HAgente detenido"
                return 0
        fi
}


status() {
        if [ $(is_running) -eq 0 ]
        then
                echo "El HAgente esta detenido"
        else
                echo "El HAgente esta ejecutandose con PID $(get_pid)"
        fi
        return 0
}


is_running() {
        ps -ef | grep "$JAR_FILE" | grep -v "grep" | wc -l
}


get_pid() {
        if [ $(is_running) -ne 0 ]
        then
                ps -ef | grep "$JAR_FILE" | grep -v "grep" | awk '{print $2}'
        else
                echo ""
        fi
}



case "$1" in
        start)
                start
                RETVAL=$?
                ;;
        stop)
                fstop
                RETVAL=$?
                ;;
        restart)
                fstop
                start
                RETVAL=$?
                ;;
        status)
                status
                RETVAL=$?
                ;;
        *)
                echo $"Usage: $0 {start|stop|restart|status}"
                RETVAL=2
esac

exit $RETVAL