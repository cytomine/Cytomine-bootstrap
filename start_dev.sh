#!/bin/bash
CORE_URL=localhost-core
IMS_URLS="[localhost-ims,localhost-ims2]"
UPLOAD_URL=localhost-upload
RETRIEVAL_URL=localhost-retrieval
IIP_OFF_URL=localhost-iip-base
IIP_VENT_URL=localhost-iip-ventana
IIP_CYTO_URL=localhost-iip-cyto
IIP_JP2_URL=localhost-iip-jp2000

HAS_GLUSTER=false
GLUSTER_SERVER=192.168.0.202
VOLUME=aurora

IS_LOCAL=true

# BACKUP_BOOL : backup active or not
BACKUP_BOOL=false
# SENDER_EMAIL, SENDER_EMAIL_PASS, SENDER_EMAIL_SMTP : email params of the sending account
# RECEIVER_EMAIL : email adress of the receiver
SENDER_EMAIL='your.email@gmail.com'
SENDER_EMAIL_PASS='passwd'
SENDER_EMAIL_SMTP='smtp.gmail.com:587'
RECEIVER_EMAIL='receiver@XXX.com'

#possible values : memory, kyoto
RETRIEVAL_ENGINE=kyoto


IMS_STORAGE_PATH=/data
IMS_BUFFER_PATH=/data/_buffer

# You don't to change the datas below this line instead of advanced customization
# ---------------------------

MEMCACHED_PASS="mypass"
# create memcached docker
docker run -d -e MEMCACHED_PASS="mypass" --name memcached1 cytomine/memcached
docker run -d -e MEMCACHED_PASS="mypass" --name memcached2 cytomine/memcached
docker run -d -e MEMCACHED_PASS="mypass" --name memcached3 cytomine/memcached
docker run -d -e MEMCACHED_PASS="mypass" --name memcached4 cytomine/memcached

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
docker run -d -p 22 -p 27017:27017 --name mongodb cytomine/mongodb

# create database docker

docker run -p 22 -m 8g -d --name retrievaldb cytomine/postgres_retrieval

docker run -p 22 -p 5432:5432 -m 8g -d --name db --volumes-from postgis_data cytomine/postgis


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

# create IIP dockers
docker run -p 22 --privileged -d --name iipOff -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached1:memcached \
-e IIP_ALIAS="iip_officiel" \
-e GLUSTER_SERVER=$GLUSTER_SERVER \
-e VOLUME=$VOLUME \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e HAS_GLUSTER=$HAS_GLUSTER \
cytomine/iipofficiel

docker run -p 22 --privileged -d --name iipCyto -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached2:memcached \
-e IIP_ALIAS="iip_cyto" \
-e GLUSTER_SERVER=$GLUSTER_SERVER \
-e VOLUME=$VOLUME \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e HAS_GLUSTER=$HAS_GLUSTER \
cytomine/iipcyto

docker run -p 22 --privileged -d --name iipVent -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached3:memcached \
-e IIP_ALIAS="iip_ventana" \
-e GLUSTER_SERVER=$GLUSTER_SERVER \
-e VOLUME=$VOLUME \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e HAS_GLUSTER=$HAS_GLUSTER \
cytomine/iipventana

docker run -p 22 --privileged -d --name iipJ2 -v $IMS_STORAGE_PATH:$IMS_STORAGE_PATH \
--link memcached4:memcached \
-e IIP_ALIAS="iip_jpeg2000" \
-e GLUSTER_SERVER=$GLUSTER_SERVER \
-e VOLUME=$VOLUME \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e HAS_GLUSTER=$HAS_GLUSTER \
cytomine/iipjpeg2000

# create retrieval docker
docker run -m 8g -d -p 22 --name retrieval --link retrievaldb:db --volumes-from retrieval_data \
-e CORE_URL=$CORE_URL \
-e IS_LOCAL=$IS_LOCAL \
-e ENGINE=$RETRIEVAL_ENGINE \
cytomine/retrieval

# create nginx docker
docker run -m 1g -d -p 22 -p 80:80 \
-v /tmp/uploaded/:/tmp/uploaded/ --link retrieval:retrieval \
--link iipOff:iip_officiel --link iipVent:iip_ventana \
--link iipCyto:iip_cyto --link iipJ2:iip_jpeg2000 \
--name nginx \
-e CORE_URL=$CORE_URL \
-e IMS_URLS="$IMS_URLS" \
-e RETRIEVAL_URL=$RETRIEVAL_URL \
-e RETRIEVAL_ALIAS=retrieval \
-e IIP_OFF_URL=$IIP_OFF_URL \
-e IIP_VENT_URL=$IIP_VENT_URL \
-e IIP_CYTO_URL=$IIP_CYTO_URL \
-e IIP_JP2_URL=$IIP_JP2_URL \
-e UPLOAD_URL=$UPLOAD_URL \
cytomine/nginx

