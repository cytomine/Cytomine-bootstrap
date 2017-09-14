#!/bin/bash
#
# Copyright (c) 2009-2017. Authors: see NOTICE file.
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

arr=$(echo $IMS_URLS | tr "," "\n")
arr=$(echo $arr | tr "[" "\n")
arr=$(echo $arr | tr "]" "\n")

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $IIP_OFF_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $IIP_CYTO_URL" >> /etc/hosts
	if [ ! -z "$IIP_JP2_URL" ]; then
		echo "$(route -n | awk '/UG[ \t]/{print $2}')       $IIP_JP2_URL" >> /etc/hosts
	fi
	for x in $arr
	do
	    echo "$(route -n | awk '/UG[ \t]/{print $2}')       $x" >> /etc/hosts
	done
fi


chown -R tomcat7:tomcat7 $IMS_STORAGE_PATH

export LD_LIBRARY_PATH=/usr/local/lib/openslide-java

mkdir -p /usr/share/tomcat7/.grails
cd /usr/share/tomcat7/.grails
touch imageserverconfig.properties
echo "dataSource.url=jdbc:h2:/tmp/devDb;MVCC=TRUE;LOCK_TIMEOUT=10000" >> imageserverconfig.properties
echo "cytomine.storagePath=$IMS_STORAGE_PATH" >> imageserverconfig.properties
echo "cytomine.storageBufferPath=$IMS_BUFFER_PATH" >> imageserverconfig.properties
echo "cytomine.imageServerPublicKey=$IMS_PUB_KEY" >> imageserverconfig.properties
echo "cytomine.imageServerPrivateKey=$IMS_PRIV_KEY" >> imageserverconfig.properties
echo "cytomine.vips=/usr/local/bin/vips" >> imageserverconfig.properties
echo "cytomine.identify=identify" >> imageserverconfig.properties
echo "cytomine.tiffinfo=tiffinfo" >> imageserverconfig.properties
echo "cytomine.vipsthumbnail=/usr/local/bin/vipsthumbnail" >> imageserverconfig.properties

echo "cytomine.iipImageServerBase=http://$IIP_OFF_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
echo "cytomine.iipImageServerCyto=http://$IIP_CYTO_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties

echo "bioformat.application.enabled=$BIOFORMAT_ENABLED" >> imageserverconfig.properties
echo "bioformat.application.location=$BIOFORMAT_LOCATION" >> imageserverconfig.properties
echo "bioformat.application.port=$BIOFORMAT_PORT" >> imageserverconfig.properties

echo "cytomine.hdf5.scriptToFindFiles=webapps/ROOT/WEB-INF/scripts/relatedFiles.sh" >> imageserverconfig.properties


if [ ! -z "$IIP_JP2_URL" ]; then
	echo "cytomine.iipImageServerJpeg2000=http://$IIP_JP2_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
	echo "cytomine.Jpeg2000Enabled=true" >> imageserverconfig.properties
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

mkdir -p /tmp/uploaded
chmod -R 777 /tmp/uploaded

tail -F /var/lib/tomcat7/logs/catalina.out
