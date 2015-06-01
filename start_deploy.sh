#!/bin/bash

#
# Copyright (c) 2009-2015. Authors: see NOTICE file.
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

if [ ! -f ./.cookies ];
then
	VALID_PWD=false
	while [ $VALID_PWD = false ]
	do
		echo "Type the password of the admin session (login : admin), followed by [ENTER]:"
		read admin_pwd

		echo "admin_pwd is $admin_pwd" # TO DELETE

		if [ -z "$admin_pwd" ] || [ $admin_pwd = "" ]
		then
			echo "password is null or empty. Enter a valid password please"
		else
			echo "length"
			echo ${#admin_pwd}
			if [ ${#admin_pwd} -lt 5 ]
			then
				echo "password is short"
			else
				echo "OK" # pas oublier de passer l'admin_pwd dans le core
			fi
			VALID_PWD=true
		fi
	done
fi

nb_docker=$(echo "$(sudo docker ps)" | wc -l)
nb_docker=$((nb_docker-1)) # remove the header line

# create memcached docker
docker run -d -e MEMCACHED_PASS="mypass" --name memcached1 cytomine/memcached
nb_docker=$((nb_docker+1))
docker run -d -e MEMCACHED_PASS="mypass" --name memcached2 cytomine/memcached
nb_docker=$((nb_docker+1))
docker run -d -e MEMCACHED_PASS="mypass" --name memcached3 cytomine/memcached
nb_docker=$((nb_docker+1))
docker run -d -e MEMCACHED_PASS="mypass" --name memcached4 cytomine/memcached
nb_docker=$((nb_docker+1))

RABBITMQ_PASS="mypass"
# create rabbitmq docker
docker run -d -p 22 -p 5672:5672 -p 15672:15672 --name rabbitmq \
-e RABBITMQ_PASS=$RABBITMQ_PASS \
cytomine/rabbitmq && nb_docker=$((nb_docker+1)) || docker start rabbitmq

# create data only containers
docker run -d --name postgis_data cytomine/data_postgis && nb_docker=$((nb_docker+1)) || docker start postgis_data

# create mongodb docker
docker run -d -p 22 --name mongodb cytomine/mongodb
nb_docker=$((nb_docker+1))

# create database docker

docker run -p 22 -m 8g -d --name db --volumes-from postgis_data cytomine/postgis
nb_docker=$((nb_docker+1))

if [ $BACKUP_BOOL = true ] 
then
	BACKUP_PATH=/backup # path (in the db container) for backup

	# create backup docker
	docker run -p 22 -d --name backup_postgis --link db:db -v /backup/postgis:$BACKUP_PATH \
	-e BACKUP_PATH=$BACKUP_PATH \
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

	docker run -p 22 -d --name backup_mongo --link mongodb:db -v /backup/mongo:$BACKUP_PATH \
	-e SGBD='mongodb' \
	-e BACKUP_PATH=$BACKUP_PATH \
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
-e IIP_ALIAS="iip_officiel" \
-e GLUSTER_SERVER=$GLUSTER_SERVER \
-e VOLUME=$VOLUME \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e HAS_GLUSTER=$HAS_GLUSTER \
cytomine/iipofficiel
nb_docker=$((nb_docker+1))

docker run -p 22 --privileged -d --name iipCyto -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached2:memcached \
-e IIP_ALIAS="iip_cyto" \
-e GLUSTER_SERVER=$GLUSTER_SERVER \
-e VOLUME=$VOLUME \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e HAS_GLUSTER=$HAS_GLUSTER \
cytomine/iipcyto
nb_docker=$((nb_docker+1))

docker run -p 22 --privileged -d --name iipVent -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached3:memcached \
-e IIP_ALIAS="iip_ventana" \
-e GLUSTER_SERVER=$GLUSTER_SERVER \
-e VOLUME=$VOLUME \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e HAS_GLUSTER=$HAS_GLUSTER \
cytomine/iipventana
nb_docker=$((nb_docker+1))

docker run -p 22 --privileged -d --name iipJ2 -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached4:memcached \
-e IIP_ALIAS="iip_jpeg2000" \
-e GLUSTER_SERVER=$GLUSTER_SERVER \
-e VOLUME=$VOLUME \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e HAS_GLUSTER=$HAS_GLUSTER \
cytomine/iipjpeg2000
nb_docker=$((nb_docker+1))

if [ $BIOFORMAT_ENABLED = true ]
then
	docker run -p 22 -d --name bioformat -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
	-e BIOFORMAT_PORT=$BIOFORMAT_PORT \
	-e BIOFORMAT_JAR_URL=$BIOFORMAT_JAR_URL \
	cytomine/bioformat
	nb_docker=$((nb_docker+1))
fi

IMS_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
IMS_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)

# create IMS docker
docker run -p 22 -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH -m 8g -d --name ims \
-v /tmp/uploaded/ \
-e IIP_OFF_URL=$IIP_OFF_URL \
-e IIP_VENT_URL=$IIP_VENT_URL \
-e IIP_CYTO_URL=$IIP_CYTO_URL \
-e IIP_JP2_URL=$IIP_JP2_URL \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e IMS_BUFFER_PATH=$IMS_BUFFER_PATH \
-e WAR_URL=$IMS_WAR_URL \
-e DOC_URL=$IMS_DOC_URL \
-e IS_LOCAL=$IS_LOCAL \
-e HAS_GLUSTER=$HAS_GLUSTER \
-e CORE_URL=$CORE_URL \
-e IMS_PUB_KEY=$IMS_PUB_KEY \
-e IMS_PRIV_KEY=$IMS_PRIV_KEY \
-e BIOFORMAT_ENABLED=$BIOFORMAT_ENABLED \
-e BIOFORMAT_LOCATION=$BIOFORMAT_ALIAS \
-e BIOFORMAT_PORT=$BIOFORMAT_PORT \
cytomine/ims
nb_docker=$((nb_docker+1))

# add a dynamic link to bioformat
if [ $BIOFORMAT_ENABLED = true ]
then
	BIOFORMAT_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' bioformat)
	docker exec ims /bin/bash -c "echo $BIOFORMAT_IP       $BIOFORMAT_ALIAS >>  /etc/hosts"
fi

ADMIN_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
ADMIN_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)
RABBITMQ_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
RABBITMQ_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)

# create CORE docker
docker run -m 8g -d -p 22 --name core --link rabbitmq:rabbitmq --link db:db --link mongodb:mongodb \
-e CORE_URL=$CORE_URL \
-e IMS_URLS=$IMS_URLS \
-e RETRIEVAL_URL=$RETRIEVAL_URL \
-e UPLOAD_URL=$UPLOAD_URL \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e IMS_BUFFER_PATH=$IMS_BUFFER_PATH \
-e WAR_URL=$CORE_WAR_URL \
-e DOC_URL=$CORE_DOC_URL \
-e IS_LOCAL=$IS_LOCAL \
-e ADMIN_PWD=$admin_pwd \
-e ADMIN_PUB_KEY=$ADMIN_PUB_KEY \
-e ADMIN_PRIV_KEY=$ADMIN_PRIV_KEY \
-e RABBITMQ_PUB_KEY=$RABBITMQ_PUB_KEY \
-e RABBITMQ_PRIV_KEY=$RABBITMQ_PRIV_KEY \
-e IMS_PUB_KEY=$IMS_PUB_KEY \
-e IMS_PRIV_KEY=$IMS_PRIV_KEY \
-e SENDER_EMAIL=$SENDER_EMAIL \
-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
-e SENDER_EMAIL_SMTP_HOST=$SENDER_EMAIL_SMTP_HOST \
-e SENDER_EMAIL_SMTP_PORT=$SENDER_EMAIL_SMTP_PORT \
cytomine/core
nb_docker=$((nb_docker+1))

# create retrieval docker
RETRIEVAL_FOLDER=/data/thumb
docker run -m 8g -d -p 22 --name retrieval \
-v $RETRIEVAL_FOLDER:$RETRIEVAL_FOLDER \
-e IMS_URLS=$IMS_URLS \
-e IS_LOCAL=$IS_LOCAL \
-e ENGINE=$RETRIEVAL_ENGINE \
-e RETRIEVAL_FOLDER=$RETRIEVAL_FOLDER \
-e RETRIEVAL_JAR_URL=$RETRIEVAL_JAR_URL \
cytomine/retrieval
nb_docker=$((nb_docker+1))

CORE_ALIAS=core
IMS_ALIAS=ims

# create nginx docker
docker run -m 1g -d -p 22 -p 80:80 --link core:$CORE_ALIAS --link ims:$IMS_ALIAS \
--volumes-from ims --link retrieval:retrieval \
--link iipOff:iip_officiel --link iipVent:iip_ventana \
--link iipCyto:iip_cyto --link iipJ2:iip_jpeg2000 \
--name nginx \
-e CORE_URL=$CORE_URL \
-e CORE_ALIAS=$CORE_ALIAS \
-e IMS_URLS="$IMS_URLS" \
-e IMS_ALIAS=$IMS_ALIAS \
-e RETRIEVAL_URL=$RETRIEVAL_URL \
-e RETRIEVAL_ALIAS=retrieval \
-e IIP_OFF_URL=$IIP_OFF_URL \
-e IIP_VENT_URL=$IIP_VENT_URL \
-e IIP_CYTO_URL=$IIP_CYTO_URL \
-e IIP_JP2_URL=$IIP_JP2_URL \
-e UPLOAD_URL=$UPLOAD_URL \
cytomine/nginx
nb_docker=$((nb_docker+1))

nb_started_docker=$(echo "$(sudo docker ps)" | wc -l)
nb_started_docker=$((nb_started_docker-1)) # remove the header line
#echo "number of started docker = $nb_started_docker"
#echo "number of asked docker = $nb_docker"
if [ $nb_started_docker -eq $nb_docker ]
then
	touch ./.cookies
else
	echo "A problem occurs. Please check into your docker logs."
fi


# wait for the admin password is setted by the core
OUTPUT_CORE_CYTOMINE=$(docker logs core)
COUNTER_CYTOMINE=0
while [ "${OUTPUT_CORE_CYTOMINE#*Server startup}" = "$OUTPUT_CORE_CYTOMINE" ] && [ $COUNTER_CYTOMINE -le 60 ]
do
   OUTPUT_CORE_CYTOMINE=$(docker logs core)
   OUTPUT_CORE_CYTOMINE=$(echo "$OUTPUT_CORE_CYTOMINE" | tail -n 50)
   COUNTER_CYTOMINE=$((COUNTER_CYTOMINE+1))
   sleep 5
done

# delete the pwd from the files & variables
docker exec core /bin/bash -c 'echo "ADMIN_PWD=" > /root/.bashrc'
docker exec core /bin/bash -c "sed -i '/adminPassword/d' /usr/share/tomcat7/.grails/cytomineconfig.groovy"
docker exec core /bin/bash -c "sed -i '/adminPrivateKey/d' /usr/share/tomcat7/.grails/cytomineconfig.groovy"
#docker exec ims /bin/bash -c "sed -i '/adminPrivateKey/d' /usr/share/tomcat7/.grails/imageserver.properties"


# create software-router docker
docker run -d -p 22 --link rabbitmq:rabbitmq \
--name software_router \
-e IS_LOCAL=$IS_LOCAL \
-e CORE_URL=$CORE_URL \
-e ALGO_TAR=$ALGO_TAR \
-e SOFTWARE_ROUTER_JAR=$SOFTWARE_ROUTER_JAR \
-e RABBITMQ_PUB_KEY=$RABBITMQ_PUB_KEY \
-e RABBITMQ_PRIV_KEY=$RABBITMQ_PRIV_KEY \
-e RABBITMQ_LOGIN=$RABBITMQ_LOGIN \
-e RABBITMQ_PASSWORD=$RABBITMQ_PASSWORD \
-e GROOVY_PATH=$GROOVY_PATH \
cytomine/software_router


if [ ! -f ./.cookies ];
then
	echo
	while true; do
	    read -p "Do you wish to install some data test? " yn
	    case $yn in
	        [Yy]* ) break;;
	        [Nn]* ) exit;;
	        * ) echo "Please answer yes or no.";;
	    esac
	done

	# create test docker
	docker run -d -p 22 \
	--name data_test \
	-e IS_LOCAL=$IS_LOCAL \
	-e CORE_URL=$CORE_URL \
	-e UPLOAD_URL=$UPLOAD_URL \
	-e PUBLIC_KEY=$ADMIN_PUB_KEY \
	-e PRIVATE_KEY=$ADMIN_PRIV_KEY \
	-e JAVA_CLIENT_JAR=$JAVA_CLIENT_JAR \
	cytomine/data_test
fi
