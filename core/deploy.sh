#!/bin/bash
echo Starting "$WAR_URL" 
#Copy the war file from mounted directory to tomcat webapps directory
if [ ! -z "$WAR_URL" ]
then
rm -r /var/lib/tomcat7/webapps/*
cd /var/lib/tomcat7/webapps/  && wget -q $WAR_URL -O ROOT.war
mkdir -p /usr/share/tomcat7/.grails
cd /usr/share/tomcat7/.grails
touch cytomineconfig.properties
echo "grails.serverURL=http://172.17.8.101:8080/" >> cytomineconfig.properties
echo "dataSource.url=jdbc:postgresql://db:5432/docker" >> cytomineconfig.properties
echo "dataSource.username=docker" >> cytomineconfig.properties
echo "dataSource.password=docker" >> cytomineconfig.properties
echo "storage_buffer=/tmp/cytomine_buffer/" >> cytomineconfig.properties
echo "storage_path=/data/beta.cytomine.be" >> cytomineconfig.properties
echo "grails.imageServerURL=http://image.cytomine.be" >> cytomineconfig.properties
echo "grails.uploadURL=http://upload.cytomine.be" >> cytomineconfig.properties
fi

service tomcat7 start

tail -F /var/lib/tomcat7/logs/catalina.out
