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
FILES=(configs/core/cytomineconfig.groovy configs/ims/ims-config.groovy configs/ims/imageserverconfig.properties configs/iipCyto/nginx.conf.sample configs/iipOff/nginx.conf.sample configs/nginx/nginx.conf configs/nginx/nginxDev.conf configs/nginx/dist/configuration.json configs/software_router/config.groovy configs/web_ui/configuration.json start_deploy.sh hosts/core/addHosts.sh hosts/ims/addHosts.sh hosts/software_router/addHosts.sh hosts/slurm/addHosts.sh)


#get all the config values.
. ./configuration.sh

POSTGRES_ALIAS=postgresql
MONGODB_ALIAS=mongodb
RABBITMQ_ALIAS=rabbitmq
CORE_ALIAS=core

if [[ $CORE_DEVELOPMENT = true ]]; then
    POSTGRES_ALIAS=localhost
    MONGODB_ALIAS=localhost
    RABBITMQ_ALIAS=localhost
    CORE_ALIAS=172.17.0.1
fi

VARIABLES=()
while read LINE; do
    if [[ $LINE == *"="* ]]; then
        IFS='=' read -ra ADDR <<< "$LINE"
        VARIABLES+=(${ADDR[0]})
    fi
done <<< "$(cat configuration.sh)"

for i in ${FILES[@]}; do
    if [ -f "$i.sample" ]; then
        cp $i.sample $i

        for j in ${VARIABLES[@]}; do
            eval sed -i "s~\\\$$j~\$$j~g" $i
        done
    fi
done

echo "Files generated."
echo "In a production environment, it's recommended to generate your own ssh keys into the configs/software_router/keys folder."
