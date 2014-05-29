#!/bin/bash

export VERBOSITY=1
export MAX_CVT=5000
export MEMCACHED_SERVERS=127.0.0.1:11211
export MEMCACHED_TIMEOUT=604800
export LOGFILE=/opt/cytomine/logs/iip-openslide.out
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9000 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9001 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9002 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9003 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9004 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9005 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9006 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9007 &

/usr/local/nginx/sbin/nginx

tail -F /usr/local/nginx/logs/access.log

