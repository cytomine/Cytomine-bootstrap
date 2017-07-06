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

#get all the config values.
. ./configuration.sh

cd base && docker build -t="cytomine/base" .
cd ../memcached && docker build -t="cytomine/memcached" .
cd ../rabbitmq && docker build -t="cytomine/rabbitmq" .
#cd ../java7 && docker build -t="cytomine/java7" .
cd ../java8 && docker build -t="cytomine/java8" .
cd ../software_router && docker build -t="cytomine/software_router" .
cd ../tomcat7 && docker build -t="cytomine/tomcat7" .
cd ../core && docker build -t="cytomine/core" .
if [ $IRIS_ENABLED = true ]
then
	cd ../iris && docker build -t="cytomine/iris" .
fi
cd ../postgres && docker build -t="cytomine/postgres" .
cd ../postgis && docker build -t="cytomine/postgis" .
cd ../retrieval && docker build -t="cytomine/retrieval" .
cd ../iipbase && docker build -t="cytomine/iipbase" .
cd ../iipCyto && docker build -t="cytomine/iipcyto" .
cd ../iipJpeg2000 && docker build -t="cytomine/iipjpeg2000" .
cd ../iipOfficial && docker build -t="cytomine/iipofficial" .
cd ../bioformat && docker build -t="cytomine/bioformat" .
cd ../ims && docker build -t="cytomine/ims" .
cd ../data_for_test && docker build -t="cytomine/data_test" .
cd ../nginx && docker build -t="cytomine/nginx" .
cd ../mongodb && docker build -t="cytomine/mongodb" .
cd ../auto_backup && docker build -t="cytomine/backup" .
cd ..
echo DONE
