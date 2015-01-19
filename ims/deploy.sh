#!/bin/bash

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
	echo "cytomine.imageServerPublicKey=4a5c7004-b6f8-4705-a118-c15d5c90dcdb" >> imageserverconfig.properties
	echo "cytomine.imageServerPrivateKey=70f35a45-c317-405a-8056-353db3d2bf56" >> imageserverconfig.properties
	echo "cytomine.vips=/usr/local/bin/vips" >> imageserverconfig.properties
	echo "cytomine.identify=identify" >> imageserverconfig.properties
	echo "cytomine.tiffinfo=tiffinfo" >> imageserverconfig.properties
	echo "cytomine.vipsthumbnail=/usr/local/bin/vipsthumbnail" >> imageserverconfig.properties

	echo "cytomine.iipImageServerBase=http://$IIP_OFF_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
	echo "cytomine.iipImageServerVentana=http://$IIP_VENT_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
	echo "cytomine.iipImageServerCyto=http://$IIP_CYTO_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
	echo "cytomine.iipImageServerJpeg2000=http://$IIP_JP2_URL/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
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
