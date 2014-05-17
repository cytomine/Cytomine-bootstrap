#!/bin/bash
cd ubuntu14_ssh && docker build -t="cytomine/base" .
cd ../java7 && docker build -t="cytomine/java7" .
cd ../tomcat7 && docker build -t="cytomine/tomcat7" .
cd ../core && docker build -t="cytomine/core" .
cd ../postgres && docker build -t="cytomine/postgis" .
echo DONE
