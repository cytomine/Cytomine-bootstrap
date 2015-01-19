#!/bin/bash

/etc/init.d/ssh start

if [ $HAS_GLUSTER = true ]; then
	#gluster mount
	mkdir /mnt/$VOLUME
	mount -t glusterfs $GLUSTER_SERVER:$VOLUME $IMS_STORAGE_PATH
fi

#nginx conf gen
sed "s/IIP_ALIAS/$IIP_ALIAS/g" /tmp/nginx.conf.sample  > /usr/local/nginx/conf/nginx.conf

export VERBOSITY=10
export MAX_CVT=10000
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

echo "/tmp/iip-openslide.out {"          > /etc/logrotate.d/iip
echo "  copytruncate"                   >> /etc/logrotate.d/iip
echo "  daily"                          >> /etc/logrotate.d/iip
echo "  rotate 14"                      >> /etc/logrotate.d/iip
echo "  compress"                       >> /etc/logrotate.d/iip
echo "  missingok"                      >> /etc/logrotate.d/iip
echo "  create 640 root root"           >> /etc/logrotate.d/iip
echo "  su root root"                   >> /etc/logrotate.d/iip
echo "}"                                >> /etc/logrotate.d/iip

mkdir /tmp/uploaded
chmod -R 777 /tmp/uploaded
chmod +x /opt/cytomine/bin/start-iip.sh
chmod +x /opt/cytomine/bin/stop-iip.sh

crontab /tmp/crontab
rm /tmp/crontab

echo "run cron"
cron

echo "start nginx"
/usr/local/nginx/sbin/nginx

tail -F /tmp/iip-openslide.out

