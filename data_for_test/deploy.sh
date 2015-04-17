#!/bin/bash
#
# Copyright (c) 2009-2015. Authors: see NOTICE file.
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

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $UPLOAD_URL" >> /etc/hosts
fi

sed -i "s/PUBLIC_KEY/$PUBLIC_KEY/g" /tmp/script.groovy
sed -i "s/PRIVATE_KEY/$PRIVATE_KEY/g" /tmp/script.groovy
sed -i "s/UPLOAD_URL/$UPLOAD_URL/g" /tmp/script.groovy
sed -i "s/CORE_URL/$CORE_URL/g" /tmp/script.groovy

if [ ! -z "$JAVA_CLIENT_JAR" ]
then
	cd /tmp/  && wget -q $JAVA_CLIENT_JAR -O Cytomine-client-java.jar
	mkdir /tmp/images && cd /tmp/images/ && wget -q "http://cytomine.be/images/test.tif" -O test.tiff

	cd /tmp/ && groovy -cp 'Cytomine-client-java.jar'  script.groovy
fi


touch /tmp/test.out

tail -F /tmp/test.out
