#!/bin/bash

#
# Copyright (c) 2009-2018. Authors: see NOTICE file.
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

mkdir -p ./reporting

docker cp retrieval:/tmp/retrieval.log ./reporting/catalinaRetrieval.out
tail -n 200 ./reporting/catalinaRetrieval.out      > ./reporting/catalinaRetrievalTail.out
mv ./reporting/catalinaRetrievalTail.out             ./reporting/catalinaRetrieval.out

docker cp iipOff:/tmp/iip-openslide.out ./reporting/logIIPOff.out
tail -n 200 ./reporting/logIIPOff.out           > ./reporting/logIIPOffTail.out
mv ./reporting/logIIPOffTail.out                  ./reporting/logIIPOff.out

docker cp iipCyto:/tmp/iip-openslide.out ./reporting/logIIPCyto.out
tail -n 200 ./reporting/logIIPCyto.out          > ./reporting/logIIPCytoTail.out
mv ./reporting/logIIPCytoTail.out                 ./reporting/logIIPCyto.out

cp configs/ims/imageserverconfig.properties ./reporting/configurationIMS.properties
docker cp ims:/var/lib/tomcat7/logs/catalina.out ./reporting/catalinaIMS.out
tail -n 500 ./reporting/catalinaIMS.out            > ./reporting/catalinaIMSTail.out
mv ./reporting/catalinaIMSTail.out                   ./reporting/catalinaIMS.out

cp configs/core/cytomineconfig.groovy ./reporting/configurationCore.groovy
docker cp core:/var/lib/tomcat7/logs/catalina.out ./reporting/catalinaCore.out
tail -n 500 ./reporting/catalinaCore.out           > ./reporting/catalinaCoreTail.out
mv ./reporting/catalinaCoreTail.out                  ./reporting/catalinaCore.out

cp ./start_deploy.sh ./reporting/start_deploy.sh

tar -zcvf reporting.tgz reporting
rm -r ./reporting
