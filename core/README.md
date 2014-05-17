# Dockerfile Tomcat7

## Info

This Dockerfile creates a container running tomcat7

## Install

- `docker build -t cytomine/tomcat7 .`

## Usage

```docker run -m 2g -d -p 8080:8080 --link core_db:db -e WAR_URL="http://192.168.1.7:8888/root.war" cytomine/core```

## Meta

Build with docker 0.11.1
