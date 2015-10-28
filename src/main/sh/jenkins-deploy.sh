#!/bin/sh
PACKAGE=$WORKSPACE/target/webcompiler-0.0.1-SNAPSHOT.war
TARGET_PACKAGE=$WORKSPACE/exec/webcompiler-0.0.1-SNAPSHOT.war
JAVA=$(which java)
ARGS="-jar $TARGET_PACKAGE --spring.profiles.active=prod"
COMMAND="$JAVA $ARGS"
PID=$(ps -ef | grep "$COMMAND" | grep -v grep | tr -s " " | cut -f2 -d" ")

# Stop application
[[ ! -z "$PID" ]] && curl -X POST http://localhost:8086/manage/shutdown

while [[ ! -z "$PID" ]]; do
    sleep 5
    PID=$(ps -ef | grep '$COMMAND' | grep -v grep | tr -s " " | cut -f2 -d" ")
done

mkdir -p $(dirname $TARGET_PACKAGE)
mv $PACKAGE $TARGET_PACKAGE

#start it
BUILD_ID=""
nohup $COMMAND > /dev/null 2>&1 &
