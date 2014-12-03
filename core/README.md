# Dockerfile Core

## Info

This Dockerfile creates a container running tomcat7 with a specified WAR in URL

## Install

`docker build -t cytomine/core .`

## Usage

```docker run -m /g -d -p 22 --link core_db:db -e WAR_URL="http://148.251.125.200:8888/core/ROOT.war" cytomine/core```
## Meta

Build with docker 1.3.0
