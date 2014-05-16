# Dockerfile Tomcat7

## Info

This Dockerfile creates a container running tomcat7

## Install

- `docker build -t cytomine/tomcat7 .`

## Usage

```docker run -d -p 8080:8080 -e WAR_URL="http://web-actions.googlecode.com/files/helloworld.war" cytomine/tomcat7```

## Meta

Build with docker 0.11.1
