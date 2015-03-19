#!/bin/bash

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

nb_docker=0


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
cytomine/rabbitmq
nb_docker=$((nb_docker+1))

# create data only containers
docker run -d --name postgis_data cytomine/data_postgis || docker start postgis_data
nb_docker=$((nb_docker+1))
#TODO mongodb
docker run -d --name retrieval_data cytomine/data_postgres || docker start retrieval_data
nb_docker=$((nb_docker+1))

# create mongodb docker
docker run -d -p 22 --name mongodb cytomine/mongodb
nb_docker=$((nb_docker+1))

# create database docker

docker run -p 22 -m 8g -d --name retrievaldb cytomine/postgres_retrieval
nb_docker=$((nb_docker+1))

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
	-e SENDER_EMAIL_SMTP=$SENDER_EMAIL_SMTP \
	-e RECEIVER_EMAIL=$RECEIVER_EMAIL \
	-e SGBD='postgres' \
	-e DATABASE='docker' \
	-e USER='docker' \
	-e PASSWD='docker' \
	cytomine/backup
	nb_docker=$((nb_docker+1))

	docker run -p 22 -d --name backup_retrieval --link retrievaldb:db -v /backup/retrieval:$BACKUP_PATH \
	-e BACKUP_PATH=$BACKUP_PATH \
	-e SENDER_EMAIL=$SENDER_EMAIL \
	-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
	-e SENDER_EMAIL_SMTP=$SENDER_EMAIL_SMTP \
	-e RECEIVER_EMAIL=$RECEIVER_EMAIL \
	-e SGBD='postgres' \
	-e DATABASE='retrieval' \
	-e USER='docker' \
	-e PASSWD='docker' \
	cytomine/backup
	nb_docker=$((nb_docker+1))

	docker run -p 22 -d --name backup_mongo --link mongodb:db -v /backup/mongo:$BACKUP_PATH \
	-e SGBD='mongodb' \
	-e BACKUP_PATH=$BACKUP_PATH \
	-e SENDER_EMAIL=$SENDER_EMAIL \
	-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
	-e SENDER_EMAIL_SMTP=$SENDER_EMAIL_SMTP \
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
-e IS_LOCAL=$IS_LOCAL \
-e HAS_GLUSTER=$HAS_GLUSTER \
-e CORE_URL=$CORE_URL \
-e IMS_PUB_KEY=$IMS_PUB_KEY \
-e IMS_PRIV_KEY=$IMS_PRIV_KEY \
-e BIOFORMAT_ENABLED=$BIOFORMAT_ENABLED \
-e BIOFORMAT_LOCATION=$BIOFORMAT_LOCATION \
-e BIOFORMAT_PORT=$BIOFORMAT_PORT \
cytomine/ims
nb_docker=$((nb_docker+1))

# create CORE docker
docker run -m 8g -d -p 22 --name core --link rabbitmq:rabbitmq --link db:db --link mongodb:mongodb \
-e CORE_URL=$CORE_URL \
-e IMS_URLS=$IMS_URLS \
-e RETRIEVAL_URL=$RETRIEVAL_URL \
-e UPLOAD_URL=$UPLOAD_URL \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e IMS_BUFFER_PATH=$IMS_BUFFER_PATH \
-e WAR_URL=$CORE_WAR_URL \
-e IS_LOCAL=$IS_LOCAL \
-e ADMIN_PWD=$admin_pwd \
-e IMS_PUB_KEY=$IMS_PUB_KEY \
-e IMS_PRIV_KEY=$IMS_PRIV_KEY \
cytomine/core
nb_docker=$((nb_docker+1))

# create retrieval docker
docker run -m 8g -d -p 22 --name retrieval --link retrievaldb:db --volumes-from retrieval_data \
-e CORE_URL=$CORE_URL \
-e IS_LOCAL=$IS_LOCAL \
-e ENGINE=$RETRIEVAL_ENGINE \
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
