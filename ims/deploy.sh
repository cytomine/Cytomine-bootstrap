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

if [ $HAS_GLUSTER = true ]; then
	#gluster mount
	mkdir /mnt/$VOLUME
	mount -t glusterfs $GLUSTER_SERVER:$VOLUME $IMS_STORAGE_PATH
fi

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $IIP_OFF_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $IIP_VENT_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $IIP_CYTO_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $IIP_JP2_URL" >> /etc/hosts
fi


chown -R tomcat7:tomcat7 $IMS_STORAGE_PATH

export LD_LIBRARY_PATH=/usr/local/lib/openslide-java

echo Starting "$WAR_URL" 
#Copy the war file from mounted directory to tomcat webapps directory
if [ ! -z "$WAR_URL" ]
then
	rm -r /var/lib/tomcat7/webapps/*
	cd /var/lib/tomcat7/webapps/ && wget -q $WAR_URL -O ROOT.war

	mkdir -p /usr/share/tomcat7/.grails
	cd /usr/share/tomcat7/.grails
	touch imageserverconfig.properties
	echo "dataSource.url=jdbc:h2:/tmp/devDb;MVCC=TRUE;LOCK_TIMEOUT=10000" >> imageserverconfig.properties
	echo "cytomine.storageBufferPath=$IMS_BUFFER_PATH" >> imageserverconfig.properties
	echo "cytomine.imageServerPublicKey=$IMS_PUB_KEY" >> imageserverconfig.properties
	echo "cytomine.imageServerPrivateKey=$IMS_PRIV_KEY" >> imageserverconfig.properties
	echo "cytomine.vips=/usr/local/bin/vips" >> imageserverconfig.properties
	echo "cytomine.identify=identify" >> imageserverconfig.properties
	echo "cytomine.tiffinfo=tiffinfo" >> imageserverconfig.properties
	echo "cytomine.vipsthumbnail=/usr/local/bin/vipsthumbnail" >> imageserverconfig.properties

	echo "cytomine.iipImageServerBase=http://$IIP_OFF_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
	echo "cytomine.iipImageServerVentana=http://$IIP_VENT_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
	echo "cytomine.iipImageServerCyto=http://$IIP_CYTO_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
	echo "cytomine.iipImageServerJpeg2000=http://$IIP_JP2_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties

	echo "bioformat.application.enabled=$BIOFORMAT_ENABLED" >> imageserverconfig.properties
	echo "bioformat.application.location=$BIOFORMAT_LOCATION" >> imageserverconfig.properties
	echo "bioformat.application.port=$BIOFORMAT_PORT" >> imageserverconfig.properties

	if [ ! -z "$DOC_URL" ]
	then
		cd /var/lib/tomcat7/  && wget -q $DOC_URL -O restapidoc.json
	fi
fi

service tomcat7 start

echo "/var/log/tomcat7/catalina.out {"   > /etc/logrotate.d/tomcat7
echo "  copytruncate"                   >> /etc/logrotate.d/tomcat7
echo "  daily"                         >> /etc/logrotate.d/tomcat7
echo "  rotate 14"                      >> /etc/logrotate.d/tomcat7
echo "  compress"                       >> /etc/logrotate.d/tomcat7
echo "  missingok"                      >> /etc/logrotate.d/tomcat7
echo "  create 640 tomcat7 adm"         >> /etc/logrotate.d/tomcat7
echo "}"                                >> /etc/logrotate.d/tomcat7

mkdir /tmp/uploaded
chmod -R 777 /tmp/uploaded

tail -F /var/lib/tomcat7/logs/catalina.out