#!/bin/bash
echo Starting "$WAR_URL" 
#Copy the war file from mounted directory to tomcat webapps directory
if [ ! -z "$WAR_URL" ]
then
rm -r /var/lib/tomcat7/webapps/*
cd /var/lib/tomcat7/webapps/  && wget -q $WAR_URL
fi

service tomcat7 start

tail -F /var/lib/tomcat7/logs/catalina.out
