#!/bin/bash
docker run -m 2g -d --name core_db cytomine/postgis
sleep 3
docker run -m 4g -d -p 8080:8080 --link core_db:db -e WAR_URL="http://192.168.1.7:8888/root.war" cytomine/core
