#!/bin/bash

#
# Copyright (c) 2009-2015. Authors: see NOTICE file.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

/etc/init.d/ssh start

echo Starting "$WAR_URL" 
#Copy the war file from mounted directory to tomcat webapps directory
if [ ! -z "$WAR_URL" ]
then
	mkdir -p /usr/share/tomcat7/.grails
	cd /usr/share/tomcat7/.grails

	cp /tmp/cytomineconfig.groovy ./

	### transform the ims urls for the config file ###
	arr=$(echo $IMS_URLS | tr "," "\n")
	arr=$(echo $arr | tr "[" "\n")
	arr=$(echo $arr | tr "]" "\n")

	IMS_URLS="["
	for x in $arr
	do
	    IMS_URLS="${IMS_URLS}'http://$x',"
	done
	IMS_URLS="${IMS_URLS%?}"
	IMS_URLS="$IMS_URLS]"

	### END transform the ims urls for the config file ###

	echo "grails.serverURL='http://$CORE_URL'" >> cytomineconfig.groovy
	echo "storage_buffer='$IMS_BUFFER_PATH'" >> cytomineconfig.groovy
	echo "storage_path='$IMS_STORAGE_PATH'" >> cytomineconfig.groovy

	echo "grails.imageServerURL=$IMS_URLS" >> cytomineconfig.groovy
	echo "grails.retrievalServerURL =['http://$RETRIEVAL_URL']" >> cytomineconfig.groovy
	echo "grails.mongo.host = 'mongodb'" >> cytomineconfig.groovy

	echo "grails.uploadURL='http://$UPLOAD_URL'" >> cytomineconfig.groovy

	echo "grails.retrievalUsername = 'cytomine'" >> cytomineconfig.groovy
	echo "grails.retrievalPassword = '$RETRIEVAL_PASSWD'" >> cytomineconfig.groovy

	echo "grails.adminPassword='$ADMIN_PWD'" >> cytomineconfig.groovy
	echo "grails.adminPrivateKey='$ADMIN_PRIV_KEY'" >> cytomineconfig.groovy
	echo "grails.adminPublicKey='$ADMIN_PUB_KEY'" >> cytomineconfig.groovy
	echo "grails.superAdminPrivateKey='$SUPERADMIN_PRIV_KEY'" >> cytomineconfig.groovy
	echo "grails.superAdminPublicKey='$SUPERADMIN_PUB_KEY'" >> cytomineconfig.groovy
	echo "grails.ImageServerPrivateKey='$IMS_PRIV_KEY'" >> cytomineconfig.groovy
	echo "grails.ImageServerPublicKey='$IMS_PUB_KEY'" >> cytomineconfig.groovy
        echo "grails.rabbitMQPrivateKey='$RABBITMQ_PRIV_KEY'" >> cytomineconfig.groovy
        echo "grails.rabbitMQPublicKey='$RABBITMQ_PUB_KEY'" >> cytomineconfig.groovy

	echo "grails.notification.email='$SENDER_EMAIL'" >> cytomineconfig.groovy
	echo "grails.notification.password='$SENDER_EMAIL_PASS'" >> cytomineconfig.groovy
	echo "grails.notification.smtp.host='$SENDER_EMAIL_SMTP_HOST'" >> cytomineconfig.groovy
	echo "grails.notification.smtp.port='$SENDER_EMAIL_SMTP_PORT'" >> cytomineconfig.groovy

	echo "grails.messageBrokerServerURL='rabbitmq:5672'" >> cytomineconfig.groovy

	rm -r /var/lib/tomcat7/webapps/*
	cd /var/lib/tomcat7/webapps/  && wget -q $WAR_URL -O ROOT.war

	if [ ! -z "$DOC_URL" ]
	then
		cd /var/lib/tomcat7/  && wget -q $DOC_URL -O restapidoc.json
		#update the basePath
		sed -i "/basePath/c\   \"basePath\": \"http://$CORE_URL\"," restapidoc.json
	fi
fi

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	for x in $arr
	do
	    echo "$(route -n | awk '/UG[ \t]/{print $2}')       $x" >> /etc/hosts
	done

	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $RETRIEVAL_URL" >> /etc/hosts
fi

mv /tmp/setenv.sh /usr/share/tomcat7/bin/

service tomcat7 start

echo "/var/log/tomcat7/catalina.out {"   > /etc/logrotate.d/tomcat7
echo "  copytruncate"                   >> /etc/logrotate.d/tomcat7
echo "  daily"                         >> /etc/logrotate.d/tomcat7
echo "  rotate 14"                      >> /etc/logrotate.d/tomcat7
echo "  compress"                       >> /etc/logrotate.d/tomcat7
echo "  missingok"                      >> /etc/logrotate.d/tomcat7
echo "  create 640 tomcat7 adm"         >> /etc/logrotate.d/tomcat7
echo "}"                                >> /etc/logrotate.d/tomcat7

tail -F /var/lib/tomcat7/logs/catalina.out
