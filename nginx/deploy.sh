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

echo "Beginning of the deployment"

sed -i "s/CORE_URL/$CORE_URL/g" /tmp/nginx.conf.sample
sed -i "s/CORE_ALIAS/core/g" /tmp/nginx.conf.sample
sed -i "s/IMS_ALIAS/ims/g" /tmp/nginx.conf.sample
sed -i "s/RETRIEVAL_URL/$RETRIEVAL_URL/g" /tmp/nginx.conf.sample

sed -i "s/IIP_OFF_URL/$IIP_OFF_URL/g" /tmp/nginx.conf.sample
sed -i "s/IIP_CYTO_URL/$IIP_CYTO_URL/g" /tmp/nginx.conf.sample
sed -i "s/IIP_JP2_URL/$IIP_JP2_URL/g" /tmp/nginx.conf.sample

sed -i "s/UPLOAD_URL/$UPLOAD_URL/g" /tmp/nginx.conf.sample


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
	sed -i "s/IMS_URLS_CONFIG/                        add_header Access-Control-Allow-Origin *; \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/			proxy_set_header Host \$host; \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/                        proxy_pass http:\/\/ims:8080; \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/                } \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IMS_URLS_CONFIG/    } \\`echo -e '\n\r'` \\`echo -e '\n\r'` IMS_URLS_CONFIG/g" /tmp/nginx.conf.sample
done
sed -i "s/IMS_URLS_CONFIG//g" /tmp/nginx.conf.sample

if [ $IRIS_ENABLED = true ]
then
	sed -i "s/IRIS_CONFIG/   server { \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                client_max_body_size 0; \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                listen       80; \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                server_name  $IRIS_URL; \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                location \/ { \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                        proxy_set_header X-Forwarded-Host \$host; \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                        proxy_set_header X-Forwarded-Server \$host; \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for; \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                        proxy_pass http:\/\/iris:8080; \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/                } \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
	sed -i "s/IRIS_CONFIG/    } \\`echo -e '\n\r'` \\`echo -e '\n\r'` IRIS_CONFIG/g" /tmp/nginx.conf.sample
fi
sed -i "s/IRIS_CONFIG//g" /tmp/nginx.conf.sample

### END transform the ims urls for the config file ###

mv /tmp/nginx.conf.sample /usr/local/nginx/conf/nginx.conf

mkdir -p /tmp/uploaded && chmod 777 /tmp/uploaded

echo "Launch of nginx"
/usr/local/nginx/sbin/nginx
echo "End of the deployment"

tail -F /usr/local/nginx/logs/access.log
