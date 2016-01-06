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

rm -r ./reporting.tgz

docker cp core:/usr/share/tomcat7/.grails/cytomineconfig.groovy ./reporting/core
docker cp core:/var/lib/tomcat7/logs/catalina.out ./reporting/core
docker cp ims:/usr/share/tomcat7/.grails/imageserverconfig.properties ./reporting/ims
docker cp ims:/var/lib/tomcat7/logs/catalina.out ./reporting/ims
docker cp retrieval:/var/lib/tomcat7/logs/catalina.out ./reporting/retrieval
docker cp iipOff:/tmp/iip-openslide.out ./reporting/iipOff
docker cp iipJ2:/tmp/iip-openslide.out ./reporting/iipJ2
docker cp iipCyto:/tmp/iip-openslide.out ./reporting/iipCyto
docker cp iipVent:/tmp/iip-openslide.out ./reporting/iipventana

mv ./reporting/core/cytomineconfig.groovy ./reporting/configurationCore.groovy
mv ./reporting/ims/imageserverconfig.properties ./reporting/configurationIMS.properties

tail -n 200 ./reporting/core/catalina.out     > ./reporting/catalinaCore.out
tail -n 200 ./reporting/ims/catalina.out      > ./reporting/catalinaIMS.out
tail -n 200 ./reporting/retrieval/catalina.out      > ./reporting/catalinaRetrieval.out

tail -n 200 ./reporting/iipOff/iip-openslide.out > ./reporting/logIIPOff.out
tail -n 200 ./reporting/iipJ2/iip-openslide.out > ./reporting/logIIPJ2.out
tail -n 200 ./reporting/iipCyto/iip-openslide.out > ./reporting/logIIPCyto.out
tail -n 200 ./reporting/iipventana/iip-openslide.out > ./reporting/logIIPVentana.out

rm -r ./reporting/core
rm -r ./reporting/ims
rm -r ./reporting/retrieval

rm -r ./reporting/iipOff
rm -r ./reporting/iipJ2
rm -r ./reporting/iipCyto
rm -r ./reporting/iipventana

cp ./configuration.sh ./reporting/configuration.sh
cp ./start_deploy.sh ./reporting/start_deploy.sh

tar -zcvf reporting.tgz reporting
rm -r ./reporting
