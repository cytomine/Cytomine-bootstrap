#!/bin/bash

echo "Beginning of the deployment"
/etc/init.d/ssh start

echo "SSH started"

sed -i "s/CORE_URL/$CORE_URL/g" /tmp/nginx.conf.sample
sed -i "s/CORE_ALIAS/$CORE_ALIAS/g" /tmp/nginx.conf.sample
sed -i "s/RETRIEVAL_URL/$RETRIEVAL_URL/g" /tmp/nginx.conf.sample
sed -i "s/RETRIEVAL_ALIAS/$RETRIEVAL_ALIAS/g" /tmp/nginx.conf.sample


IMS_URLS_CONFIG=""
### transform the ims urls for the config file ###
arr=$(echo $IMS_URLS | tr "," "\n")
arr=$(echo $arr | tr "[" "\n")
arr=$(echo $arr | tr "]" "\n")

for x in $arr
do
	sed -i "s/IMS_URLS_CONFIG/   server { \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/                client_max_body_size 0; \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/                listen       80; \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/                server_name  $x; \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/                location \/ { \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/			proxy_set_header Host \$host; \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/                        proxy_pass http:\/\/$IMS_ALIAS:8080; \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/                } \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/    } \\`echo -e '\n\r'` \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
done
sed -i "s/IMS_URLS_CONFIG//g" /tmp/nginx.conf.sample

### END transform the ims urls for the config file ###



mv /tmp/nginx.conf.sample /etc/nginx/nginx.conf

echo "Launch of nginx"
nginx
echo "End of the deployment"

tail -F /var/log/nginx/access.log
