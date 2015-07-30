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

#get all the config values.
. ./configuration.sh

if [ ! -z "$CORE_WAR_URL" ]
then
	docker exec core service tomcat7 stop
	docker exec core pkill -U tomcat7
	docker exec core rm /var/lib/tomcat7/webapps/ROOT.war
	docker exec core wget $CORE_WAR_URL -O /var/lib/tomcat7/webapps/ROOT.war

	if [ ! -z "$CORE_DOC_URL" ]
	then
		docker exec core wget $CORE_DOC_URL -O /var/lib/tomcat7/restapidoc.json
		#update the basePath
		docker exec core sed -i "/basePath/c\   \"basePath\": \"http://$CORE_URL\"," /var/lib/tomcat7/restapidoc.json
	fi
fi

docker exec core service tomcat7 start


