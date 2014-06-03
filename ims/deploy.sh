#!/bin/bash

/etc/init.d/ssh start

chown -R tomcat7.tomcat7 /var/docker_vol

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
echo "grails.storageBufferPath=$IMS_BUFFER_PATH" >> imageserverconfig.properties
echo "grails.imageServerPublicKey=4a5c7004-b6f8-4705-a118-c15d5c90dcdb" >> imageserverconfig.properties
echo "grails.imageServerPrivateKey=70f35a45-c317-405a-8056-353db3d2bf56" >> imageserverconfig.properties
fi
service tomcat7 start

export VERBOSITY=1
export MAX_CVT=5000
#export MEMCACHED_SERVERS=127.0.0.1:11211
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

/usr/local/nginx/sbin/nginx

tail -F /var/lib/tomcat7/logs/catalina.out
