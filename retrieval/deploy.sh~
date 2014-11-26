#!/bin/bash

/etc/init.d/ssh start

echo "Starting WAR deployment"
#Copy the war file to tomcat webapps directory
rm -r /var/lib/tomcat7/webapps/*
cd /var/lib/tomcat7/webapps/  && cp /tmp/retrieval-web.war retrieval-web.war

mkdir -p /usr/share/tomcat7/.grails
cd /usr/share/tomcat7/.grails

touch retrievalwebconfig.properties
echo "#############################" > retrievalwebconfig.properties
echo "retrieval.path.config=/usr/share/tomcat7/.grails/config" >> retrievalwebconfig.properties
echo "retrieval.standalone=false" >> retrievalwebconfig.properties
#echo "retrieval.provider.host=http://beta.cytomine.be" >> retrievalwebconfig.properties
echo "retrieval.provider.host=http://$CORE_URL" >> retrievalwebconfig.properties
echo "retrieval.provider.login=4a5c7004-b6f8-4705-a118-c15d5c90dcdb" >> retrievalwebconfig.properties
echo "retrieval.provider.pass=70f35a45-c317-405a-8056-353db3d2bf56" >> retrievalwebconfig.properties
echo "retrieval.provider.collection=http://$CORE_URL/api/userannotation.json" >> retrievalwebconfig.properties
echo "retrieval.provider.container=http://$CORE_URL/api/project.json" >> retrievalwebconfig.properties
echo "retrieval.purge.cron=0 0 1 1 * ? " >> retrievalwebconfig.properties
echo "dataSource.url=jdbc:postgresql://db:5432/retrieval" >> retrievalwebconfig.properties
echo "dataSource.username=docker" >> retrievalwebconfig.properties
echo "dataSource.password=docker" >> retrievalwebconfig.properties
echo "#############################" >> retrievalwebconfig.properties

cp -r /tmp/testsvectors testsvectors
cp -r /tmp/config config

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL" >> /etc/hosts
fi

service tomcat7 start

tail -F /var/lib/tomcat7/logs/catalina.out
