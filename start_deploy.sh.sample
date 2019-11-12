#!/bin/bash

#
# Copyright (c) 2009-2018. Authors: see NOTICE file.
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

docker create --name memcached \
--restart=unless-stopped \
cytomine/memcached:v1.1.2 > /dev/null

docker cp $PWD/configs/memcached/memcached.conf memcached:/etc/memcached.conf
docker start memcached


docker create --name rabbitmq \
-p 5672:5672 -p 15672:15672 \
-e RABBITMQ_PASS=$RABBITMQ_PASS \
--restart=unless-stopped \
cytomine/rabbitmq:v1.1.2 > /dev/null

docker start rabbitmq


docker volume create --name postgis_data > /dev/null
# create database docker
docker run -d -m 8g --name postgresql -v postgis_data:/var/lib/postgresql \
--restart=unless-stopped \
cytomine/postgis:v2.0.0 > /dev/null

docker volume create --name mongodb_data > /dev/null
# create mongodb docker
docker run -d --name mongodb -v mongodb_data:/data/db \
--restart=unless-stopped \
cytomine/mongodb:v1.1.2 > /dev/null

docker create --name iipOff \
--link memcached:memcached \
-v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--privileged -e NB_IIP_PROCESS=$NB_IIP_PROCESS \
--restart=unless-stopped \
cytomine/iipofficial:v1.2.0 > /dev/null

docker cp $PWD/configs/iipOff/nginx.conf.sample iipOff:/tmp/nginx.conf.sample
docker start iipOff


docker create --name iipCyto \
--link memcached:memcached \
-v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--privileged -e NB_IIP_PROCESS=$NB_IIP_PROCESS \
--restart=unless-stopped \
cytomine/iipcyto:v1.2.1 > /dev/null

docker cp $PWD/configs/iipCyto/nginx.conf.sample iipCyto:/tmp/nginx.conf.sample
docker start iipCyto


docker create --name bioformat \
-v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
-e BIOFORMAT_PORT=$BIOFORMAT_PORT \
--restart=unless-stopped \
cytomine/bioformat:v1.1.2 > /dev/null

docker start bioformat


docker create --name ims \
--link bioformat:bioformat \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
-v $IMS_BUFFER_PATH:/tmp/uploaded \
--restart=unless-stopped \
cytomine/ims:v1.2.2 > /dev/null

docker cp $PWD/configs/ims/imageserverconfig.properties ims:/usr/share/tomcat7/.grails/imageserverconfig.properties
docker cp $PWD/hosts/ims/addHosts.sh ims:/tmp/addHosts.sh
docker start ims


docker create --name core \
--link postgresql:postgresql \
--link mongodb:mongodb \
--link rabbitmq:rabbitmq \
-v /etc/localtime:/etc/localtime \
--restart=unless-stopped \
cytomine/core:v2.0.0 > /dev/null

docker cp $PWD/configs/core/cytomineconfig.groovy core:/usr/share/tomcat7/.grails/cytomineconfig.groovy
docker cp $PWD/hosts/core/addHosts.sh core:/tmp/addHosts.sh
docker start core


docker create --name nginx \
--link ims:ims \
--link iipCyto:iipCyto \
--link core:core \
--link iipOff:iipOff \
-v $IMS_BUFFER_PATH:/tmp/uploaded \
-p 80:80 \
--restart=unless-stopped \
cytomine/nginx:v1.2.0 > /dev/null

docker cp $PWD/configs/nginx/nginx.conf nginx:/usr/local/nginx/conf/nginx.conf
docker cp $PWD/configs/nginx/dist nginx:/tmp/.
docker start nginx


# wait for the admin password is setted by the core
OUTPUT_CORE_CYTOMINE=$(sudo docker exec core tail -n 200 /var/lib/tomcat7/logs/catalina.out 2> /dev/null)
COUNTER_CYTOMINE=0
while [ "${OUTPUT_CORE_CYTOMINE#*Server startup}" = "$OUTPUT_CORE_CYTOMINE" ] && [ $COUNTER_CYTOMINE -le 720 ]
do
   OUTPUT_CORE_CYTOMINE=$(sudo docker exec core tail -n 200 /var/lib/tomcat7/logs/catalina.out 2> /dev/null)
   COUNTER_CYTOMINE=$((COUNTER_CYTOMINE+1))
   sleep 5
done
if [ "${OUTPUT_CORE_CYTOMINE#*Server startup}" = "$OUTPUT_CORE_CYTOMINE" ]
then
   echo "An error occured. Core Cytomine is too long to start. Please contact support for more details."
else
   echo "Core Cytomine launched."
fi

docker create --name software_router \
--link rabbitmq:rabbitmq \
-v $ALGO_PATH:/software_router/algo/ \
--privileged \
-e CORE_URL=http://$CORE_URL \
-e RABBITMQ_PUB_KEY=$RABBITMQ_PUB_KEY \
-e RABBITMQ_PRIV_KEY=$RABBITMQ_PRIV_KEY \
--restart=unless-stopped \
cytomine/software_router:v1.1.1 > /dev/null

docker cp $PWD/hosts/software_router/addHosts.sh software_router:/tmp/addHosts.sh
docker cp $PWD/configs/software_router/config.groovy software_router:/software_router/config.groovy
docker start software_router


