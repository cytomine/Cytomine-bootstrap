#!/bin/bash

/etc/init.d/ssh start

#gluster mount
mkdir /mnt/$VOLUME
mount -t glusterfs $GLUSTER_SERVER:$VOLUME /mnt/$VOLUME

#nginx conf gen
sed "s/IIP_URL/$IIP_URL/g" /tmp/nginx.conf.sample  > /tmp/out.tmp1
sed "s/UPLOAD_URL/$UPLOAD_URL/g" /tmp/out.tmp1 > /usr/local/nginx/conf/nginx.conf
rm /tmp/out.tmp1



if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL $IIP_URL" >> /etc/hosts
fi


chown -R tomcat7:tomcat7 $IMS_STORAGE_PATH

export LD_LIBRARY_PATH=/usr/local/lib/openslide-java

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
echo "cytomine.storageBufferPath=$IMS_BUFFER_PATH" >> imageserverconfig.properties
echo "cytomine.imageServerPublicKey=4a5c7004-b6f8-4705-a118-c15d5c90dcdb" >> imageserverconfig.properties
echo "cytomine.imageServerPrivateKey=70f35a45-c317-405a-8056-353db3d2bf56" >> imageserverconfig.properties
echo "cytomine.vips=/usr/local/bin/vips" >> imageserverconfig.properties
echo "cytomine.identify=identify" >> imageserverconfig.properties
echo "cytomine.tiffinfo=tiffinfo" >> imageserverconfig.properties
echo "cytomine.vipsthumbnail=/usr/local/bin/vipsthumbnail" >> imageserverconfig.properties
echo "cytomine.iipImageServer=http://$IIP_URL:81/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
#echo "cytomine.iipImageServer=http://$IIP_URL:80/fcgi-bin/iipsrv.fcgi" >> imageserverconfig.properties
fi
service tomcat7 start

export VERBOSITY=1
export MAX_CVT=5000
export MEMCACHED_SERVERS=memcached:11211
export MEMCACHED_TIMEOUT=604800
export LOGFILE=/tmp/iip-openslide.out
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9000 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9001 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9002 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9003 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9004 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9005 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9006 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9007 &

mkdir /tmp/uploaded
chmod -R 777 /tmp/uploaded

/usr/local/nginx/sbin/nginx

tail -F /var/lib/tomcat7/logs/catalina.out
