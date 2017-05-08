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

#get all the config values.
. ./configuration.sh

DATA_INSERTION=false
docker volume inspect postgis_data > /dev/null 2> /dev/null
if test $? -eq 1;
then
	VALID_PWD=false
	while [ $VALID_PWD = false ]
	do
		echo "Type the password of the admin session (login : admin), followed by [ENTER]:"
		read admin_pwd

		echo "admin_pwd has been set"

		if [ -z "$admin_pwd" ] || [ $admin_pwd = "" ] || [ ${#admin_pwd} -lt 5 ]
		then
			echo "password is null, empty or too short. Enter a valid password please"
		else
			echo "OK"
			VALID_PWD=true
			DATA_INSERTION=true
		fi
	done
fi

nb_docker=$(echo "$(sudo docker ps)" | wc -l)
nb_docker=$((nb_docker-1)) # remove the header line

# create memcached docker
docker run -d -e MEMCACHED_PASS="mypass" --name memcached1 --restart=unless-stopped cytomine/memcached > /dev/null
nb_docker=$((nb_docker+1))
docker run -d -e MEMCACHED_PASS="mypass" --name memcached2 --restart=unless-stopped cytomine/memcached > /dev/null
nb_docker=$((nb_docker+1))

RABBITMQ_PASS="mypass"
# create rabbitmq docker
docker run -d -p 22 -p 5672:5672 -p 15672:15672 --name rabbitmq --restart=unless-stopped \
-e RABBITMQ_PASS=$RABBITMQ_PASS \
cytomine/rabbitmq  > /dev/null
nb_docker=$((nb_docker+1))

# create data volumes
docker volume create --name postgis_data > /dev/null
docker volume create --name mongodb_data > /dev/null

if [ $IRIS_ENABLED = true ]
then
	docker volume create --name iris_data > /dev/null
fi

# create mongodb docker
docker run -d -p 22 --name mongodb -v mongodb_data:/data/db --restart=unless-stopped cytomine/mongodb > /dev/null
nb_docker=$((nb_docker+1))

# create database docker
docker run -d -p 22 -m 8g --name db -v postgis_data:/var/lib/postgresql --restart=unless-stopped cytomine/postgis > /dev/null
nb_docker=$((nb_docker+1))

if [ $BACKUP_BOOL = true ] 
then
	# create backup docker
	docker run -p 22 -d --name backup_postgis --link db:db -v $BACKUP_PATH/postgis:/backup --restart=unless-stopped \
	-e SENDER_EMAIL=$SENDER_EMAIL \
	-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
	-e SENDER_EMAIL_SMTP_HOST=$SENDER_EMAIL_SMTP_HOST \
	-e SENDER_EMAIL_SMTP_PORT=$SENDER_EMAIL_SMTP_PORT \
	-e RECEIVER_EMAIL=$RECEIVER_EMAIL \
	-e SGBD='postgres' \
	-e DATABASE='docker' \
	-e USER='docker' \
	-e PASSWD='docker' \
	cytomine/backup > /dev/null
	nb_docker=$((nb_docker+1))

	docker run -p 22 -d --name backup_mongo --link mongodb:db -v $BACKUP_PATH/mongo:/backup --restart=unless-stopped \
	-e SGBD='mongodb' \
	-e SENDER_EMAIL=$SENDER_EMAIL \
	-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
	-e SENDER_EMAIL_SMTP_HOST=$SENDER_EMAIL_SMTP_HOST \
	-e SENDER_EMAIL_SMTP_PORT=$SENDER_EMAIL_SMTP_PORT \
	-e RECEIVER_EMAIL=$RECEIVER_EMAIL \
	cytomine/backup > /dev/null
	nb_docker=$((nb_docker+1))
fi

# create IIP dockers
# privileged for somaxconn
docker run -p 22 --privileged -d --name iipOff -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH --restart=unless-stopped \
--link memcached1:memcached \
-e NB_IIP_PROCESS=10 \
cytomine/iipofficial > /dev/null
nb_docker=$((nb_docker+1))

docker run -p 22 --privileged -d --name iipCyto -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH --restart=unless-stopped \
--link memcached2:memcached \
-e NB_IIP_PROCESS=10 \
cytomine/iipcyto > /dev/null
nb_docker=$((nb_docker+1))

if [ $BIOFORMAT_ENABLED = true ]
then
	docker run -p 22 -d --name bioformat -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH --restart=unless-stopped \
	-e BIOFORMAT_PORT=$BIOFORMAT_PORT \
	cytomine/bioformat > /dev/null
	nb_docker=$((nb_docker+1))
fi

IMS_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
IMS_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)

# create IMS docker
docker run -p 22 -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH -m 8g -d --name ims --restart=unless-stopped \
-v /tmp/uploaded/ \
-e IIP_OFF_URL=$IIP_OFF_URL \
-e IIP_CYTO_URL=$IIP_CYTO_URL \
-e IMS_URLS=$IMS_URLS \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e IMS_BUFFER_PATH=$IMS_BUFFER_PATH \
-e IS_LOCAL=$IS_LOCAL \
-e CORE_URL=$CORE_URL \
-e IMS_PUB_KEY=$IMS_PUB_KEY \
-e IMS_PRIV_KEY=$IMS_PRIV_KEY \
-e BIOFORMAT_ENABLED=$BIOFORMAT_ENABLED \
-e BIOFORMAT_LOCATION=$BIOFORMAT_ALIAS \
-e BIOFORMAT_PORT=$BIOFORMAT_PORT \
cytomine/ims > /dev/null
nb_docker=$((nb_docker+1))

# add a dynamic link to bioformat
if [ $BIOFORMAT_ENABLED = true ]
then
	BIOFORMAT_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' bioformat)
	docker exec ims /bin/bash -c "echo $BIOFORMAT_IP       $BIOFORMAT_ALIAS >>  /etc/hosts"
fi

ADMIN_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
ADMIN_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)
SUPERADMIN_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
SUPERADMIN_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)
RABBITMQ_PUB_KEY=$(cat /proc/sys/kernel/random/uuid)
RABBITMQ_PRIV_KEY=$(cat /proc/sys/kernel/random/uuid)

# create CORE docker
docker run -m 8g -d -p 22 --name core --link rabbitmq:rabbitmq --link db:db --link mongodb:mongodb --restart=unless-stopped \
-e CORE_URL=$CORE_URL \
-e IMS_URLS=$IMS_URLS \
-e RETRIEVAL_URL=$RETRIEVAL_URL \
-e UPLOAD_URL=$UPLOAD_URL \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e IMS_BUFFER_PATH=$IMS_BUFFER_PATH \
-e IS_LOCAL=$IS_LOCAL \
-e ADMIN_PWD=$admin_pwd \
-e ADMIN_PUB_KEY=$ADMIN_PUB_KEY \
-e ADMIN_PRIV_KEY=$ADMIN_PRIV_KEY \
-e SUPERADMIN_PUB_KEY=$SUPERADMIN_PUB_KEY \
-e SUPERADMIN_PRIV_KEY=$SUPERADMIN_PRIV_KEY \
-e RABBITMQ_PUB_KEY=$RABBITMQ_PUB_KEY \
-e RABBITMQ_PRIV_KEY=$RABBITMQ_PRIV_KEY \
-e IMS_PUB_KEY=$IMS_PUB_KEY \
-e IMS_PRIV_KEY=$IMS_PRIV_KEY \
-e RETRIEVAL_PASSWD=$RETRIEVAL_PASSWD \
-e SENDER_EMAIL=$SENDER_EMAIL \
-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
-e SENDER_EMAIL_SMTP_HOST=$SENDER_EMAIL_SMTP_HOST \
-e SENDER_EMAIL_SMTP_PORT=$SENDER_EMAIL_SMTP_PORT \
cytomine/core > /dev/null
nb_docker=$((nb_docker+1))

# create retrieval docker
docker run -m 8g -d -p 22 --name retrieval --restart=unless-stopped \
-v $RETRIEVAL_PATH:/data/thumb \
-e IMS_URLS=$IMS_URLS \
-e IS_LOCAL=$IS_LOCAL \
-e ENGINE=$RETRIEVAL_ENGINE \
-e RETRIEVAL_PASSWD=$RETRIEVAL_PASSWD \
cytomine/retrieval > /dev/null
nb_docker=$((nb_docker+1))

if [ $IRIS_ENABLED = true ]
then
	# create IRIS docker
	docker run -d -p 22 --name iris --restart=unless-stopped \
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
	cytomine/iris > /dev/null
	nb_docker=$((nb_docker+1))
fi

# create nginx docker
#if iris is not linked, nginx doesn't start. No other way for a condition. :/
if [ $IRIS_ENABLED = true ]
then
	docker run -m 1g -d -p 22 -p 80:80 --link core:core --link ims:ims \
	--volumes-from ims --link retrieval:retrieval \
	--link iipOff:iip_official \
	--link iipCyto:iip_cyto \
	--link iris:iris \
	--name nginx \
	--restart=unless-stopped \
	-e CORE_URL=$CORE_URL \
	-e IMS_URLS="$IMS_URLS" \
	-e RETRIEVAL_URL=$RETRIEVAL_URL \
	-e IIP_OFF_URL=$IIP_OFF_URL \
	-e IIP_CYTO_URL=$IIP_CYTO_URL \
	-e UPLOAD_URL=$UPLOAD_URL \
	-e IRIS_URL=$IRIS_URL \
	-e IRIS_ENABLED=$IRIS_ENABLED \
	cytomine/nginx > /dev/null
else
	docker run -m 1g -d -p 22 -p 80:80 --link core:core --link ims:ims \
	--volumes-from ims --link retrieval:retrieval \
	--link iipOff:iip_official \
	--link iipCyto:iip_cyto \
	--name nginx \
	--restart=unless-stopped \
	-e CORE_URL=$CORE_URL \
	-e IMS_URLS="$IMS_URLS" \
	-e RETRIEVAL_URL=$RETRIEVAL_URL \
	-e IIP_OFF_URL=$IIP_OFF_URL \
	-e IIP_CYTO_URL=$IIP_CYTO_URL \
	-e UPLOAD_URL=$UPLOAD_URL \
	-e IRIS_ENABLED=$IRIS_ENABLED \
	cytomine/nginx > /dev/null
fi
nb_docker=$((nb_docker+1))


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

# delete the pwd from the files & variables
docker exec core /bin/bash -c 'echo "ADMIN_PWD=" > /root/.bashrc'
docker exec core /bin/bash -c "sed -i '/adminPassword/d' /usr/share/tomcat7/.grails/cytomineconfig.groovy"
docker exec core /bin/bash -c "sed -i '/adminPrivateKey/d' /usr/share/tomcat7/.grails/cytomineconfig.groovy"
#docker exec ims /bin/bash -c "sed -i '/adminPrivateKey/d' /usr/share/tomcat7/.grails/imageserver.properties"



# create software-router docker
docker run -d -p 22 --link rabbitmq:rabbitmq \
--privileged \
--name software_router --restart=unless-stopped \
-v $ALGO_PATH:/software_router/algo/ \
-e IS_LOCAL=$IS_LOCAL \
-e CORE_URL=$CORE_URL \
-e IMS_URLS=$IMS_URLS \
-e UPLOAD_URL=$UPLOAD_URL \
-e RABBITMQ_PUB_KEY=$RABBITMQ_PUB_KEY \
-e RABBITMQ_PRIV_KEY=$RABBITMQ_PRIV_KEY \
-e RABBITMQ_LOGIN=$RABBITMQ_LOGIN \
-e RABBITMQ_PASSWORD=$RABBITMQ_PASSWORD \
cytomine/software_router > /dev/null
nb_docker=$((nb_docker+1))



if [ $DATA_INSERTION = true ]
then
	echo 
	while true; do
	    read -p "Do you wish to install some data test? y/n (It can take 45 minutes depending of your internet connexion.) " yn
	    case $yn in
	        [Yy]* ) 
			# create test docker
			docker run -d -p 22 \
			--name data_test \
			-e IS_LOCAL=$IS_LOCAL \
			-e CORE_URL=$CORE_URL \
			-e IMS_URLS=$IMS_URLS \
			-e UPLOAD_URL=$UPLOAD_URL \
			-e PUBLIC_KEY=$SUPERADMIN_PUB_KEY \
			-e PRIVATE_KEY=$SUPERADMIN_PRIV_KEY \
			cytomine/data_test > /dev/null
			nb_docker=$((nb_docker+1))

			echo "Data test in installation."
			break
			;;
	        [Nn]* ) 
			DATA_INSERTION=false
			break
			;;
	        * ) echo "Please answer yes or no.";;
	    esac
	done

fi


# checking
running_containers=$(sudo docker ps)
nb_started_docker=$(echo "$running_containers" | wc -l)
nb_started_docker=$((nb_started_docker-1)) # remove the header line
#echo "number of started docker = $nb_started_docker"
#echo "number of asked docker = $nb_docker"
if [ ! $nb_started_docker -eq $nb_docker ]
then
	if ! echo "$running_containers" | grep -q -w nginx; then echo "nginx container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w core; then echo "core container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w ims; then echo "ims container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w db; then echo "db container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w mongodb; then echo "mongodb container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w memcached1; then echo "memcached1 container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w memcached2; then echo "memcached2 container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w rabbitmq; then echo "rabbitmq container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w iipOff; then echo "iipOff container is not running !"; fi
	if ! echo "$running_containers" | grep -q -w iipCyto; then echo "iipCyto container is not running !"; fi
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


if [ $DATA_INSERTION = true ]
then
	OUTPUT_DATA_CYTOMINE=$(docker logs data_test)
	COUNTER_CYTOMINE=0
	while [ "${OUTPUT_DATA_CYTOMINE#*END OF DATA INJECTION}" = "$OUTPUT_DATA_CYTOMINE" ] && [ $COUNTER_CYTOMINE -le 45 ]
	do
	   OUTPUT_DATA_CYTOMINE=$(docker logs data_test)
	   OUTPUT_DATA_CYTOMINE=$(echo "$OUTPUT_DATA_CYTOMINE" | tail -n 100)
	   COUNTER_CYTOMINE=$((COUNTER_CYTOMINE+1))
	   sleep 60
	done
	if [ "${OUTPUT_DATA_CYTOMINE#*DATA SUCCESSFULLY INJECTED}" = "$OUTPUT_DATA_CYTOMINE" ]
	then
	   echo "Data are not plainfully injected. Please check the status with the command docker logs data_test."
	else
	   echo "Data successfully injected."
	fi
fi
echo "End of the installation."

