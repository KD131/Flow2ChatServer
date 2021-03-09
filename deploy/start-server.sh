#!/bin/sh

java -cp chat-server.jar ChatServer 46.101.246.201 1234 log/logfile.log
#original IP was "0.0.0.0", currently IP and port have been changed to fit our program