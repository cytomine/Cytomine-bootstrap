#!/bin/bash

# create database docker
docker run -p 22 -m 512m -d --name db cytomine/postgis

# create IMS docker
docker run -p 81:80 -v /var/docker_vol -p 22 -m 512m -d --name ims -e WAR_URL="http://192.168.1.6:8888/ims/root.war" cytomine/ims

# create CORE docker
docker run -m 2g -d -p 22 --name core --link db:db --link ims:ims -e WAR_URL="http://192.168.1.6:8888/core/root.war" cytomine/core

# create nginx docker
docker run -m 256m -d -p 80:80 --link ims:ims --link core:core cytomine/nginx
