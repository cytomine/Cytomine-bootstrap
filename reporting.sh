#!/bin/bash

#
# Copyright (c) 2009-2020. Authors: see NOTICE file.
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

cp configs/pims/pims-config.env ./reporting/pims-config.env

cp configs/core/application.yml ./reporting/application.yml
docker cp core:/var/lib/tomcat7/logs/catalina.out ./reporting/catalinaCore.out
tail -n 500 ./reporting/catalinaCore.out           > ./reporting/catalinaCoreTail.out
mv ./reporting/catalinaCoreTail.out                  ./reporting/catalinaCore.out

cp configs/nginx/nginx.conf ./reporting/nginx.conf

cp ./start_deploy.sh ./reporting/start_deploy.sh

tar -zcvf reporting.tgz reporting
rm -r ./reporting
