#!/bin/bash
cd ubuntu14_ssh
docker build -t="cytomine/base" .
cd ../java8
docker build -t="cytomine/java8" .
cd ../tomcat7
docker build -t="cytomine/tomcat7" .
cd ..
echo DONE
