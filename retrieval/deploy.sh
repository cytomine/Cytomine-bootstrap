#!/bin/bash

#/etc/init.d/ssh start

apt-get update
apt-get install -y wget

mkdir -p /tmp/retrieval/
cd /tmp/retrieval/  && wget --no-check-certificate $RETRIEVAL_JAR_URL -O retrieval.zip

apt-get install unzip
unzip retrieval.zip

mkdir -p $RETRIEVAL_FOLDER

echo "lauch retrieval"

cd CBIRest-0.2.0/

cp -r /tmp/testsvectors testsvectors
cp -r /tmp/config config

redis-server&

java -jar retrieval-0.2-SNAPSHOT.war --spring.profiles.active=prod --retrieval.store.name=REDIS --retrieval.thumb.index=$RETRIEVAL_FOLDER/index --retrieval.thumb.search=$RETRIEVAL_FOLDER/search

touch test.out
tail -F /tmp/test.out
