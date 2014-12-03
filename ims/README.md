# Dockerfile Core

## Info

This Dockerfile creates a container running tomcat7 with a specified WAR in URL

## Install

`docker build -t cytomine/ims .`

## Usage

```docker run -m 8g -d -p 8080:8080 -e LD_LIBRARY_PATH=/usr/local/lib/openslide-java -e WAR_URL="http://148.251.125.200:8888/ims/ROOT.war" cytomine/ims```
## Meta

Build with docker 1.3.0
