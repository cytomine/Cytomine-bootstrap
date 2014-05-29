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
echo "grails.serverURL='http://aurora_core.cytomine.be'" >> cytomineconfig.groovy
echo "dataSource.url='jdbc:postgresql://db:5432/docker'" >> cytomineconfig.groovy
echo "dataSource.username='docker'" >> cytomineconfig.groovy
echo "dataSource.password='docker'" >> cytomineconfig.groovy
echo "storage_buffer='/tmp/cytomine_buffer/'" >> cytomineconfig.groovy
echo "storage_path='/var/docker_vol'" >> cytomineconfig.groovy
echo "grails.imageServerURL='http://aurora_ims.cytomine.be:81'" >> cytomineconfig.groovy
echo "grails.uploadURL='http://aurora_upload.cytomine.be:81'" >> cytomineconfig.groovy
fi

service tomcat7 start

tail -F /var/lib/tomcat7/logs/catalina.out
