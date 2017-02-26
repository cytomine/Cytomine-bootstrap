#!/bin/bash
#
# Copyright (c) 2009-2017. Authors: see NOTICE file.
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

arr=$(echo $IMS_URLS | tr "," "\n")
arr=$(echo $arr | tr "[" "\n")
arr=$(echo $arr | tr "]" "\n")

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $UPLOAD_URL" >> /etc/hosts
	for x in $arr
	do
	    echo "$(route -n | awk '/UG[ \t]/{print $2}')       $x" >> /etc/hosts
	done
fi

mv /tmp/injectdata.groovy /tmp/script/

cd /tmp/script/ && groovy -cp '../Cytomine-client-java.jar' injectdata.groovy ./ http://$CORE_URL http://$x http://$UPLOAD_URL $PUBLIC_KEY $PRIVATE_KEY


touch /tmp/test.out

tail -F /tmp/test.out
