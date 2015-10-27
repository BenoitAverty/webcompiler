#!/bin/sh
JAVA=$(which java)
ARGS="-jar $WORKSPACE/target/webcompiler-0.0.1-SNAPSHOT.war --spring.profiles.active=prod"
COMMAND="$JAVA $ARGS"
PID=$(ps -ef | grep '$COMMAND' | grep -v grep | tr -s " " | cut -f2 -d" ")

# Stop application
curl -X POST http://localhost:8086/manage/shutdown

while [[ ! -z "$PID" ]]; do
    sleep 5
    PID=$(ps -ef | grep '$COMMAND' | grep -v grep | tr -s " " | cut -f2 -d" ")
done

#start it
BUILD_ID=""
nohup $COMMAND > /dev/null 2>&1 &
