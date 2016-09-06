#!/bin/bash

#
# Copyright (c) 2009-2016. Authors: see NOTICE file.
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

#get all the config values.
. ./configuration.sh

nb_docker=$(echo "$(sudo docker ps)" | wc -l)
nb_docker=$((nb_docker-1)) # remove the header line

# create memcached docker
docker run -d -e MEMCACHED_PASS="mypass" --name memcached1 cytomine/memcached
nb_docker=$((nb_docker+1))
docker run -d -e MEMCACHED_PASS="mypass" --name memcached2 cytomine/memcached
nb_docker=$((nb_docker+1))
docker run -d -e MEMCACHED_PASS="mypass" --name memcached3 cytomine/memcached
nb_docker=$((nb_docker+1))

RABBITMQ_PASS="mypass"
# create rabbitmq docker
docker run -d -p 22 -p 5672:5672 -p 15672:15672 --name rabbitmq \
-e RABBITMQ_PASS=$RABBITMQ_PASS \
cytomine/rabbitmq && nb_docker=$((nb_docker+1)) || docker start rabbitmq

# create data volumes
docker volume create --name postgis_data
docker volume create --name mongodb_data
if [ $IRIS_ENABLED = true ]
then
	docker volume create --name iris_data
fi

# create mongodb docker
docker run -d -p 22 -p 27017:27017 --name mongodb -v mongodb_data:/var/lib/mongodb cytomine/mongodb
nb_docker=$((nb_docker+1))

# create database docker
docker run -d -p 22 -p 5432:5432 -m 8g --name db -v postgis_data:/var/lib/postgresql cytomine/postgis
nb_docker=$((nb_docker+1))

if [ $BACKUP_BOOL = true ] 
then
	# create backup docker
	docker run -p 22 -d --name backup_postgis --link db:db -v $BACKUP_PATH/postgis:/backup \
	-e BACKUP_PATH=/backup \
	-e SENDER_EMAIL=$SENDER_EMAIL \
	-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
	-e SENDER_EMAIL_SMTP_HOST=$SENDER_EMAIL_SMTP_HOST \
	-e SENDER_EMAIL_SMTP_PORT=$SENDER_EMAIL_SMTP_PORT \
	-e RECEIVER_EMAIL=$RECEIVER_EMAIL \
	-e SGBD='postgres' \
	-e DATABASE='docker' \
	-e USER='docker' \
	-e PASSWD='docker' \
	cytomine/backup
	nb_docker=$((nb_docker+1))

	docker run -p 22 -d --name backup_mongo --link mongodb:db -v $BACKUP_PATH/mongo:/backup \
	-e SGBD='mongodb' \
	-e BACKUP_PATH=/backup \
	-e SENDER_EMAIL=$SENDER_EMAIL \
	-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
	-e SENDER_EMAIL_SMTP_HOST=$SENDER_EMAIL_SMTP_HOST \
	-e SENDER_EMAIL_SMTP_PORT=$SENDER_EMAIL_SMTP_PORT \
	-e RECEIVER_EMAIL=$RECEIVER_EMAIL \
	cytomine/backup
	nb_docker=$((nb_docker+1))
fi

# create IIP dockers
docker run -p 22 --privileged -d --name iipOff -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached1:memcached \
-e NB_IIP_PROCESS=10 \
cytomine/iipofficiel
nb_docker=$((nb_docker+1))

docker run -p 22 --privileged -d --name iipCyto -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached2:memcached \
-e NB_IIP_PROCESS=10 \
cytomine/iipcyto
nb_docker=$((nb_docker+1))

docker run -p 22 --privileged -d --name iipJ2 -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached3:memcached \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
cytomine/iipjpeg2000
nb_docker=$((nb_docker+1))

if [ $BIOFORMAT_ENABLED = true ]
then
	docker run -p 22 -d --name bioformat -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
	-e BIOFORMAT_PORT=$BIOFORMAT_PORT \
	cytomine/bioformat
	nb_docker=$((nb_docker+1))
fi

IMS_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
IMS_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)
ADMIN_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
ADMIN_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)
SUPERADMIN_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
SUPERADMIN_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)
RABBITMQ_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
RABBITMQ_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)

# create retrieval docker
docker run -m 8g -d -p 22 --name retrieval \
-v $RETRIEVAL_PATH:/data/thumb \
-e IMS_URLS=$IMS_URLS \
-e IS_LOCAL=$IS_LOCAL \
-e ENGINE=$RETRIEVAL_ENGINE \
-e RETRIEVAL_FOLDER=/data/thumb \
-e RETRIEVAL_PASSWD=$RETRIEVAL_PASSWD \
cytomine/retrieval
nb_docker=$((nb_docker+1))

if [ $IRIS_ENABLED = true ]
then
	# create IRIS docker
	docker run -d -p 22 --name iris \
	-v iris_data:/var/lib/tomcat7/db \
	-e CORE_URL=$CORE_URL \
	-e IMS_URLS=$IMS_URLS \
	-e IS_LOCAL=$IS_LOCAL \
	-e IRIS_URL=$IRIS_URL \
	-e IRIS_ID=$IRIS_ID \
	-e SENDER_EMAIL=$SENDER_EMAIL \
	-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
	-e SENDER_EMAIL_SMTP_HOST=$SENDER_EMAIL_SMTP_HOST \
	-e IRIS_ADMIN_NAME="$IRIS_ADMIN_NAME" \
	-e IRIS_ADMIN_ORGANIZATION_NAME="$IRIS_ADMIN_ORGANIZATION_NAME" \
	-e IRIS_ADMIN_EMAIL="$IRIS_ADMIN_EMAIL" \
	cytomine/iris
	nb_docker=$((nb_docker+1))
fi

# create nginx docker
#if iris is not linked, nginx doesn't start. No other way for a condition. :/
if [ $IRIS_ENABLED = true ]
then
	docker run -m 1g -d -p 22 -p 80:80 \
	-v /tmp/uploaded/:/tmp/uploaded/ --link retrieval:retrieval \
	--link iipOff:iip_officiel \
	--link iipCyto:iip_cyto --link iipJ2:iip_jpeg2000 \
	--link iris:iris \
	--name nginx \
	-e CORE_URL=$CORE_URL \
	-e IMS_URLS="$IMS_URLS" \
	-e RETRIEVAL_URL=$RETRIEVAL_URL \
	-e IIP_OFF_URL=$IIP_OFF_URL \
	-e IIP_VENT_URL=$IIP_VENT_URL \
	-e IIP_CYTO_URL=$IIP_CYTO_URL \
	-e IIP_JP2_URL=$IIP_JP2_URL \
	-e UPLOAD_URL=$UPLOAD_URL \
	-e IRIS_URL=$IRIS_URL \
	-e IRIS_ENABLED=$IRIS_ENABLED \
	cytomine/nginx
else
	docker run -m 1g -d -p 22 -p 80:80 \
	-v /tmp/uploaded/:/tmp/uploaded/ --link retrieval:retrieval \
	--link iipOff:iip_officiel \
	--link iipCyto:iip_cyto --link iipJ2:iip_jpeg2000 \
	--name nginx \
	-e CORE_URL=$CORE_URL \
	-e IMS_URLS="$IMS_URLS" \
	-e RETRIEVAL_URL=$RETRIEVAL_URL \
	-e IIP_OFF_URL=$IIP_OFF_URL \
	-e IIP_VENT_URL=$IIP_VENT_URL \
	-e IIP_CYTO_URL=$IIP_CYTO_URL \
	-e IIP_JP2_URL=$IIP_JP2_URL \
	-e UPLOAD_URL=$UPLOAD_URL \
	-e IRIS_ENABLED=$IRIS_ENABLED \
	cytomine/nginx
fi
nb_docker=$((nb_docker+1))


# create software-router docker
docker run -d -p 22 --link rabbitmq:rabbitmq \
--name software_router \
-v $MODELS_PATH:/software_router/algo/models/ \
-e IS_LOCAL=$IS_LOCAL \
-e CORE_URL=$CORE_URL \
-e IMS_URLS=$IMS_URLS \
-e RABBITMQ_PUB_KEY=$RABBITMQ_PUB_KEY \
-e RABBITMQ_PRIV_KEY=$RABBITMQ_PRIV_KEY \
-e RABBITMQ_LOGIN=$RABBITMQ_LOGIN \
-e RABBITMQ_PASSWORD=$RABBITMQ_PASSWORD \
-e GROOVY_PATH=$GROOVY_PATH \
cytomine/software_router
nb_docker=$((nb_docker+1))



# checking
running_containers=$(sudo docker ps)
nb_started_docker=$(echo "$running_containers" | wc -l)
nb_started_docker=$((nb_started_docker-1)) # remove the header line
#echo "number of started docker = $nb_started_docker"
#echo "number of asked docker = $nb_docker"
if [ $nb_started_docker -eq $nb_docker ]
then
        touch ./.cookies
else
	if ! echo "$running_containers" | grep -q -w iris_data; then echo "iris_data container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w mongodb_data; then echo "mongodb_data container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w postgis_data; then echo "postgis_data container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w nginx; then echo "nginx container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w db; then echo "db container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w mongodb; then echo "mongodb container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w memcached1; then echo "memcached1 container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w memcached2; then echo "memcached2 container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w memcached3; then echo "memcached3 container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w memcached4; then echo "memcached4 container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w rabbitmq; then echo "rabbitmq container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w iipOff; then echo "iipOff container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w iipCyto; then echo "iipCyto container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w iipJ2; then echo "iipJ2 container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w retrieval; then echo "retrieval container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w software_router; then echo "software_router container is not running !"; fi

	if [ $BACKUP_BOOL = true ] 
	then
		if ! echo "$running_containers" | grep -q -w backup_postgis; then echo "backup_postgis container is not running !"; fi
		if ! echo "$running_containers" | grep -q -w backup_mongo; then echo "backup_mongo container is not running !"; fi
	fi
	if [ $BIOFORMAT_ENABLED = true ]
	then
		if ! echo "$running_containers" | grep -q -w bioformat; then echo "bioformat container is not running !"; fi
	fi
	if [ $IRIS_ENABLED = true ]
	then
		if ! echo "$running_containers" | grep -q -w iris; then echo "iris container is not running !"; fi
	fi
        echo "Please check into your docker logs."
        #echo "A problem occurs. Please check into your docker logs."
fi

