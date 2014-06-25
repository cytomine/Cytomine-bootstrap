#!/bin/bash

/etc/init.d/ssh start

echo Starting "$WAR_URL" 
#Copy the war file from mounted directory to tomcat webapps directory
if [ ! -z "$WAR_URL" ]
then
rm -r /var/lib/tomcat7/webapps/*
cd /var/lib/tomcat7/webapps/  && wget -q $WAR_URL -O ROOT.war
mkdir -p /usr/share/tomcat7/.grails
cd /usr/share/tomcat7/.grails
touch cytomineconfig.groovy
echo "grails.serverURL='http://$CORE_URL'" >> cytomineconfig.groovy
echo "dataSource.url='jdbc:postgresql://db:5432/docker'" >> cytomineconfig.groovy
echo "dataSource.username='docker'" >> cytomineconfig.groovy
echo "dataSource.password='docker'" >> cytomineconfig.groovy
echo "storage_buffer='$IMS_BUFFER_PATH'" >> cytomineconfig.groovy
echo "storage_path='$IMS_STORAGE_PATH'" >> cytomineconfig.groovy
echo "grails.imageServerURL='http://$IMS_URL'" >> cytomineconfig.groovy
echo "grails.uploadURL='http://$UPLOAD_URL:81'" >> cytomineconfig.groovy
echo "grails.admin.client='info@cytomine.be'" >> cytomineconfig.groovy
echo "grails.integration.aurora.url='http://localhost:8000/api/image/notify.json?test=true'" >> cytomineconfig.groovy
echo "grails.integration.aurora.username='xxx'" >> cytomineconfig.groovy
echo "grails.integration.aurora.password='xxx'" >> cytomineconfig.groovy
echo "grails.integration.aurora.interval='60000'" >> cytomineconfig.groovy
fi

service tomcat7 start

tail -F /var/lib/tomcat7/logs/catalina.out
