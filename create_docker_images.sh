#!/bin/bash
cd base && docker build -t="cytomine/base" .
cd ../postgresql_datastore && docker build -t="cytomine/postgresql_datastore" .
cd ../memcached && docker build -t="cytomine/memcached" .
cd ../dnsmasq && docker build -t="cytomine/dnsmasq" .
cd ../rabbitmq && docker build -t="cytomine/rabbitmq" .
cd ../java7 && docker build -t="cytomine/java7" .
cd ../tomcat7 && docker build -t="cytomine/tomcat7" .
cd ../core && docker build -t="cytomine/core" .
cd ../postgres && docker build -t="cytomine/postgres" .
cd ../postgres_retrieval && docker build -t="cytomine/postgres_retrieval" .
cd ../postgis && docker build -t="cytomine/postgis" .
cd ../retrieval && docker build -t="cytomine/retrieval" .
cd ../iip && docker build -t="cytomine/iip" .
cd ../ims && docker build -t="cytomine/ims" .
cd ../nginx && docker build -t="cytomine/nginx" .
docker build -t="cytomine/mongodb" github.com/dockerfile/mongodb
cd ../auto_backup && docker build -t="cytomine/backup" .
cd ../data_containers/postgres && docker build -t="cytomine/data_postgres" .
cd ../postgis && docker build -t="cytomine/data_postgis" .
cd ../..
echo DONE
