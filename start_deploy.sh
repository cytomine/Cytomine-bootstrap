#!/bin/bash
CORE_WAR_URL="http://148.251.125.200:8888/core/ROOT.war"
IMS_WAR_URL="http://148.251.125.200:8888/ims/ROOT.war"
CORE_URL=localhost-core #aurora.cytomine.be
CORE_ALIAS=core #used in nginx config
IMS_URL=localhost-ims #aurora-ims.cytomine.be
IMS_ALIAS=ims #used in nginx config
IIP_URL=localhost-iip #aurora-iip.cytomine.be
UPLOAD_URL=localhost-upload #aurora-upload.cytomine.be
RETRIEVAL_URL=localhost-retrieval
IMS_STORAGE_PATH=/mnt/aurora
IMS_BUFFER_PATH=/mnt/aurora/_buffer
RABBITMQ_PASS="mypass"
MEMCACHED_PASS="mypass"

# create postgresql_datastore docker
#docker run -d -p 22 --name aurora_db_datastore -v /mnt/aurora/database:/backup cytomine/postgresql_datastore 

# create memcached docker
docker run -d -p 11211:11211 -e MEMCACHED_PASS="mypass" --name memcached cytomine/memcached

# create rabbitmq docker
docker run -d -p 22 -p 5672:5672 -p 15672:15672 --name rabbitmq \
-e RABBITMQ_PASS=$RABBITMQ_PASS \
cytomine/rabbitmq

# create mongodb docker
docker run -d -p 22 --name mongodb cytomine/mongodb

# create database docker

docker run -p 22 -m 8g -d --name retrievaldb cytomine/postgres_retrieval

BACKUP_PATH=/backup # path (in the db container) for backup

# BACKUP_BOOL : backup active or not
# SENDER_EMAIL, SENDER_EMAIL_PASS, SENDER_EMAIL_SMTP : email params of the sending account
# RECEIVER_EMAIL : email adress of the receiver
docker run -p 22 -m 8g -d --name db -v /backup:$BACKUP_PATH \
-e BACKUP_BOOL=false \
-e BACKUP_PATH=$BACKUP_PATH \
-e SENDER_EMAIL='your.email@gmail.com' \
-e SENDER_EMAIL_PASS='passwd' \
-e SENDER_EMAIL_SMTP='smtp.gmail.com:587' \
-e RECEIVER_EMAIL='receiver@XXX.com' \
cytomine/postgis

# create database backup docker
#docker run -d -p 22 --name aurora_backup -v /mnt/aurora/database:/backup  cytomine/postgresql_datastore 

# create IMS docker
docker run -p 22 --privileged -p 81:80 -v /mnt/aurora:$IMS_STORAGE_PATH -m 8g -d --name ims --link memcached:memcached \
-e IIP_URL=$IIP_URL \
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
docker run -m 8g -d -p 22 --name retrieval --link retrievaldb:db \
-e CORE_URL=$CORE_URL \
-e IS_LOCAL=true \
cytomine/retrieval

# create nginx docker
docker run -m 1g -d -p 22 -p 80:80 --link core:$CORE_ALIAS --link ims:$IMS_ALIAS --link retrieval:retrieval \
-e CORE_URL=$CORE_URL \
-e CORE_ALIAS=$CORE_ALIAS \
-e IMS_URL=$IMS_URL \
-e IMS_ALIAS=$IMS_ALIAS \
-e RETRIEVAL_URL=$RETRIEVAL_URL \
-e RETRIEVAL_ALIAS=retrieval \
cytomine/nginx

