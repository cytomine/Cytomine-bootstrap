#!/bin/bash

#
# Copyright (c) 2009-2016. Authors: see NOTICE file.
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

mkdir -p /usr/share/tomcat7/.grails #(make directory for externalized config in the home of the user which will run the tomcat server)

### transform the ims urls for the config file ###
arr=$(echo $IMS_URLS | tr "," "\n")
arr=$(echo $arr | tr "[" "\n")
arr=$(echo $arr | tr "]" "\n")

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL" >> /etc/hosts
	for x in $arr
	do
	    echo "$(route -n | awk '/UG[ \t]/{print $2}')       $x" >> /etc/hosts
	done
fi



#—> alter the configs according to your deployment environment, server name, port, Cytomine core server connection etc.
sed -i "s/IRIS_URL/$IRIS_URL/g" /tmp/iris-production-config.groovy
sed -i "s/IRIS_ID/$IRIS_ID/g" /tmp/iris-production-config.groovy
sed -i "s/SENDER_EMAIL_PASS/$SENDER_EMAIL_PASS/g" /tmp/iris-production-config.groovy
sed -i "s/SENDER_EMAIL_SMTP_HOST/$SENDER_EMAIL_SMTP_HOST/g" /tmp/iris-production-config.groovy
sed -i "s/SENDER_EMAIL/$SENDER_EMAIL/g" /tmp/iris-production-config.groovy

sed -i "s/CORE_URL/$CORE_URL/g" /tmp/iris-config.groovy
sed -i "s/IMS_URL/$x/g" /tmp/iris-config.groovy
sed -i "s/IRIS_ADMIN_NAME/$IRIS_ADMIN_NAME/g" /tmp/iris-config.groovy
sed -i "s/IRIS_ADMIN_ORGANIZATION_NAME/$IRIS_ADMIN_ORGANIZATION_NAME/g" /tmp/iris-config.groovy
sed -i "s/IRIS_ADMIN_EMAIL/$IRIS_ADMIN_EMAIL/g" /tmp/iris-config.groovy


cp /tmp/iris-* /usr/share/tomcat7/.grails


chmod -R 777 /var/lib/tomcat7/ # can create /var/lib/tomcat7/db. Find a more elegant solution later

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
