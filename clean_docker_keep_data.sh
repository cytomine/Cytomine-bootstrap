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
docker stop memcached
docker rm -v memcached
docker stop rabbitmq
docker rm -v rabbitmq
docker stop mongodb
docker rm -v mongodb
docker stop postgresql
docker rm -v postgresql
docker stop backup_mongo
docker rm -v backup_mongo
docker stop backup_postgis
docker rm -v backup_postgis
docker stop slurm
docker rm -v slurm
docker stop iipOff
docker rm -v iipOff
docker stop iipCyto
docker rm -v iipCyto
docker stop bioformat
docker rm -v bioformat
docker stop ims
docker rm -v ims
docker stop core
docker rm -v core
docker stop web_UI
docker rm -v web_UI
docker stop nginx
docker rm -v nginx
docker stop software_router
docker rm -v software_router
