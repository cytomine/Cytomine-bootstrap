#!/bin/bash
#
# Copyright (c) 2009-2016. Authors: see NOTICE file.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

/etc/init.d/ssh start

apt-get update
apt-get install -y wget

mkdir -p /tmp/retrieval/
cd /tmp/retrieval/  && wget --no-check-certificate $RETRIEVAL_JAR_URL -O retrieval.zip

apt-get install unzip
unzip retrieval.zip

mkdir -p $RETRIEVAL_FOLDER

echo "lauch retrieval"

cd CBIRest-0.2.1/

cp -r /tmp/testsvectors testsvectors
cp -r /tmp/config config

touch password.txt
echo "cytomine:$RETRIEVAL_PASSWD" > password.txt


### transform the ims urls for the config file ###
arr=$(echo $IMS_URLS | tr "," "\n")
arr=$(echo $arr | tr "[" "\n")
arr=$(echo $arr | tr "]" "\n")

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	for x in $arr
	do
	    echo "$(route -n | awk '/UG[ \t]/{print $2}')       $x" >> /etc/hosts
	done
fi



touch /tmp/retrieval.log
if [ "$ENGINE" == "memory" ] 
then
	java -jar retrieval-0.2.1-SNAPSHOT.war --spring.profiles.active=prod --retrieval.store.name=MEMORY --retrieval.thumb.index=$RETRIEVAL_FOLDER/index --retrieval.thumb.search=$RETRIEVAL_FOLDER/search
else
	cd $RETRIEVAL_FOLDER && redis-server /tmp/redis.conf&

	java -jar retrieval-0.2.1-SNAPSHOT.war --spring.profiles.active=prod --retrieval.store.name=REDIS --retrieval.thumb.index=$RETRIEVAL_FOLDER/index --retrieval.thumb.search=$RETRIEVAL_FOLDER/search > /tmp/retrieval.log
fi

tail -F /tmp/retrieval.log
