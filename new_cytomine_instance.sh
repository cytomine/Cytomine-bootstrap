#!/bin/bash
CORE_WAR_URL=http://192.168.1.2:8888/ims/root.war
IMS_WAR_URL=http://192.168.1.2:8888/core/root.war
CORE_URL=aurora_core.cytmine.be #aurora.cytomine.be
IMS_URL=aurora_ims.cytomine.be #aurora-ims.cytomine.be
IIP_URL=aurora_iip.cytomine.be #aurora-iip.cytomine.be
UPLOAD_URL=aurora_upload.cytomine.be #aurora-upload.cytomine.be
IMS_STORAGE_PATH=/var/docker_vol #/mnt/aurora
IMS_BUFFER_PATH=/tmp/imageserver_buffer #/mnt/aurora/_buffer
RABBITMQ_PASS="mypass" 

# create rabbitmq docker
docker run -d -p 5672:5672 -p 15672:15672 --name rabbitmq -e RABBITMQ_PASS=$RABBITMQ_PASS cytomine/rabbitmq

# create database docker
docker run -p 22 -m 512m -d --name db cytomine/postgis

# create IMS docker
docker run -p 81:80 -v /var/docker_vol -p 22 -m 512m -d --name ims  \
-e IIP_URL=$IIP_URL \
-e UPLOAD_URL=$UPLOAD_URL \
-e IMS_BUFFER_PATH=$IMS_BUFFER_PATH \
-e WAR_URL=$CORE_WAR_URL \
cytomine/ims

# create CORE docker
docker run -m 2g -d -p 22 --name core --link rabbitmq:rabbitmq --link db:db --link ims:ims \
-e CORE_URL=$CORE_URL \
-e IMS_URL=$IMS_URL \
-e IIP_URL=$IIP_URL \
-e UPLOAD_URL=$UPLOAD_URL \
-e IMS_STORAGE_PATH=$IMS_STORAGE_PATH \
-e IMS_BUFFER_PATH=IMS_BUFFER_PATH \
-e WAR_URL="http://192.168.1.2:8888/core/root.war" \
cytomine/core

# create nginx docker
docker run -m 256m -d -p 80:80 --link core:core --link ims:ims \
-e CORE_URL=$CORE_URL \
-e IMS_URL=$IMS_URL \
cytomine/nginx
