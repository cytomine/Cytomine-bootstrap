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

	cd /tmp/ && wget "http://cytomine.be/release/demo_script.zip" -O demo_script.zip && unzip demo_script.zip

	cd /tmp/script/ && groovy -cp '../Cytomine-client-java.jar' injectdata.groovy ./ http://$CORE_URL http://$UPLOAD_URL $PUBLIC_KEY $PRIVATE_KEY
fi


touch /tmp/test.out

tail -F /tmp/test.out
