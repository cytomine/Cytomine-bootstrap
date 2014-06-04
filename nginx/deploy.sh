#!/bin/bash

/etc/init.d/ssh start

sed "s/CORE_URL/$CORE_URL/g" /tmp/nginx.conf.sample  > /tmp/out.tmp1
sed "s/IMS_URL/$IMS_URL/g" /tmp/out.tmp1 > /etc/nginx/nginx.conf
rm /tmp/out.tmp1

nginx

tail -F /var/log/nginx/access.log
