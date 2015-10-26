#!/bin/sh
COMMAND="java -jar target/webcompiler-0.0.1-SNAPSHOT.war --spring.profiles.active=prod"

# Kill running process
kill $(ps -ef | grep $COMMAND | grep -v grep | tr -s " " | cut -f2 -d" ")

#start it
nohup $COMMAND &
