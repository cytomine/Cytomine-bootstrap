#!/bin/bash
CORE_URL=localhost-core
IMS_URL=localhost-ims
IIP_URL=localhost-iip
UPLOAD_URL=localhost-upload
RETRIEVAL_URL=localhost-retrieval

# BACKUP_BOOL : backup active or not
BACKUP_BOOL=false
# SENDER_EMAIL, SENDER_EMAIL_PASS, SENDER_EMAIL_SMTP : email params of the sending account
# RECEIVER_EMAIL : email adress of the receiver
SENDER_EMAIL='your.email@gmail.com'
SENDER_EMAIL_PASS='passwd'
SENDER_EMAIL_SMTP='smtp.gmail.com:587'
RECEIVER_EMAIL='receiver@XXX.com'



# You don't to change the datas below this line instead of advanced customization
# ---------------------------


CORE_WAR_URL="http://148.251.125.200:8888/core/ROOT.war"
IMS_WAR_URL="http://148.251.125.200:8888/ims/ROOT.war"
IMS_STORAGE_PATH=/mnt/aurora
IMS_BUFFER_PATH=/mnt/aurora/_buffer

MEMCACHED_PASS="mypass"
# create memcached docker
docker run -d -p 11211:11211 -e MEMCACHED_PASS="mypass" --name memcached cytomine/memcached

RABBITMQ_PASS="mypass"
# create rabbitmq docker
docker run -d -p 22 -p 5672:5672 -p 15672:15672 --name rabbitmq \
-e RABBITMQ_PASS=$RABBITMQ_PASS \
cytomine/rabbitmq

# create data only containers
docker run -d --name postgis_data cytomine/data_postgis
#TODO mongodb
docker run -d --name retrieval_data cytomine/data_postgres

# create mongodb docker
docker run -d -p 22 --name mongodb cytomine/mongodb

# create database docker

docker run -p 22 -m 8g -d --name retrievaldb cytomine/postgres_retrieval

docker run -p 22 -m 8g -d --name db --volumes-from postgis_data cytomine/postgis


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

	docker run -p 22 -d --name backup_mongo --link mongodb:db -v /backup/mongo:$BACKUP_PATH \
	-e SGBD='mongodb' \
	-e BACKUP_PATH=$BACKUP_PATH \
	-e SENDER_EMAIL=$SENDER_EMAIL \
	-e SENDER_EMAIL_PASS=$SENDER_EMAIL_PASS \
	-e SENDER_EMAIL_SMTP=$SENDER_EMAIL_SMTP \
	-e RECEIVER_EMAIL=$RECEIVER_EMAIL \
	cytomine/backup
fi

IIP_ALIAS=iip
# create IIP docker
docker run -p 22 -d --name iip -v /mnt/aurora:$IMS_STORAGE_PATH --link memcached:memcached \
-e IIP_ALIAS=$IIP_ALIAS \
cytomine/iip

# create IMS docker
docker run -p 22 --privileged -p 81:80 -v /mnt/aurora:$IMS_STORAGE_PATH -m 8g -d --name ims --link iip:iip \
-e IIP_URL=$IIP_URL \
-e IIP_ALIAS=$IIP_ALIAS \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e UPLOAD_URL=$UPLOAD_URL \
-e IMS_BUFFER_PATH=$IMS_BUFFER_PATH \
-e GLUSTER_SERVER=192.168.0.202 \
-e VOLUME=aurora \
-e WAR_URL=$IMS_WAR_URL \
-e IS_LOCAL=true \
-e CORE_URL=$CORE_URL \
cytomine/ims

# create CORE docker
docker run -m 8g -d -p 22 --name core --link rabbitmq:rabbitmq --link db:db --link mongodb:mongodb \
-e CORE_URL=$CORE_URL \
-e IMS_URL=$IMS_URL \
-e RETRIEVAL_URL=$RETRIEVAL_URL \
-e UPLOAD_URL=$UPLOAD_URL \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e IMS_BUFFER_PATH=$IMS_BUFFER_PATH \
-e WAR_URL=$CORE_WAR_URL \
-e IS_LOCAL=true \
cytomine/core

# create retrieval docker
docker run -m 8g -d -p 22 --name retrieval --link retrievaldb:db --volumes-from retrieval_data \
-e CORE_URL=$CORE_URL \
-e IS_LOCAL=true \
cytomine/retrieval

CORE_ALIAS=core 
IMS_ALIAS=ims 

# create nginx docker
docker run -m 1g -d -p 22 -p 80:80 --link core:$CORE_ALIAS --link ims:$IMS_ALIAS --link retrieval:retrieval \
--name nginx \
-e CORE_URL=$CORE_URL \
-e CORE_ALIAS=$CORE_ALIAS \
-e IMS_URL=$IMS_URL \
-e IMS_ALIAS=$IMS_ALIAS \
-e RETRIEVAL_URL=$RETRIEVAL_URL \
-e RETRIEVAL_ALIAS=retrieval \
cytomine/nginx

