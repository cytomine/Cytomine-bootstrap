#!/bin/bash
cd base && docker build -t="cytomine/base" .
cd ../memcached && docker build -t="cytomine/memcached" .
cd ../dnsmasq && docker build -t="cytomine/dnsmasq" .
cd ../rabbitmq && docker build -t="cytomine/rabbitmq" .
cd ../java7 && docker build -t="cytomine/java7" .
cd ../tomcat7 && docker build -t="cytomine/tomcat7" .
cd ../core && docker build -t="cytomine/core" .
cd ../postgis && docker build -t="cytomine/postgis" .
cd ../ims && docker build -t="cytomine/ims" .
cd ../nginx && docker build -t="cytomine/nginx" .
echo DONE
