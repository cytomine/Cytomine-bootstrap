#!/bin/bash
echo Starting "$WAR_URL" 
#Copy the war file from mounted directory to tomcat webapps directory
if [ ! -z "$WAR_URL" ]
then
rm -r /var/lib/tomcat7/webapps/*
cd /var/lib/tomcat7/webapps/  && wget -q $WAR_URL -O ROOT.war
mkdir -p /usr/share/tomcat7/.grails
cd /usr/share/tomcat7/.grails
touch imageserverconfig.properties
echo "dataSource.url=jdbc:h2:/tmp/devDb;MVCC=TRUE;LOCK_TIMEOUT=10000" >> imageserverconfig.properties
#cat "grails.storageBufferPath=/tmp/imageserver_buffer" > imageserverconfig.properties
#cat "grails.imageServerPublicKey=4a5c7004-b6f8-4705-a118-c15d5c90dcdb" > imageserverconfig.properties
#cat "grails.imageServerPrivateKey=70f35a45-c317-405a-8056-353db3d2bf56" > imageserverconfig.properties
fi

service tomcat7 start

tail -F /var/lib/tomcat7/logs/catalina.out
