#!/bin/bash

echo "Beginning of the deployment"
/etc/init.d/ssh start

echo "SSH started"

sed "s/CORE_URL/$CORE_URL/g" /tmp/nginx.conf.sample  > /tmp/out.tmp1
sed "s/CORE_ALIAS/$CORE_ALIAS/g" /tmp/out.tmp1  > /tmp/out.tmp2
sed "s/IMS_URL/$IMS_URL/g" /tmp/out.tmp2 > /tmp/out.tmp3
sed "s/IMS_ALIAS/$IMS_ALIAS/g" /tmp/out.tmp3 > /tmp/out.tmp4
sed "s/RETRIEVAL_URL/$RETRIEVAL_URL/g" /tmp/out.tmp4 > /tmp/out.tmp5
sed "s/RETRIEVAL_ALIAS/$RETRIEVAL_ALIAS/g" /tmp/out.tmp5 > /etc/nginx/nginx.conf
rm /tmp/out.tmp1
rm /tmp/out.tmp2
rm /tmp/out.tmp3
rm /tmp/out.tmp4
rm /tmp/out.tmp5

echo "Launch of nginx"
nginx
echo "End of the deployment"

tail -F /var/log/nginx/access.log
