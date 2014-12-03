# Dockerfile Core

## Info

This Dockerfile creates a container running tomcat7 with a specified WAR in URL

## Install

`docker build -t cytomine/core .`

## Usage

```docker run -m 2g -d -p 8080:8080 -e LD_LIBRARY_PATH=/usr/local/lib/openslide-java -e WAR_URL="http://192.168.1.8:8888/ims/root.war" cytomine/ims```

## Meta

Build with docker 0.11.1
