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

CORE_URL=localhost-core
IMS_URLS="[localhost-ims,localhost-ims2]"
UPLOAD_URL=localhost-upload
RETRIEVAL_URL=localhost-retrieval
IIP_OFF_URL=localhost-iip-base
IIP_VENT_URL=localhost-iip-ventana
IIP_CYTO_URL=localhost-iip-cyto
IIP_JP2_URL=localhost-iip-jp2000

IS_LOCAL=true

# BACKUP_BOOL : backup active or not
BACKUP_BOOL=false
# SENDER_EMAIL, SENDER_EMAIL_PASS, SENDER_EMAIL_SMTP : email params of the sending account
# RECEIVER_EMAIL : email adress of the receiver
SENDER_EMAIL='your.email@gmail.com'
SENDER_EMAIL_PASS='passwd'
SENDER_EMAIL_SMTP_HOST='smtp.gmail.com'
SENDER_EMAIL_SMTP_PORT='587'
RECEIVER_EMAIL='receiver@XXX.com'

#possible values : memory, redis
RETRIEVAL_ENGINE=redis
RETRIEVAL_PASSWD='retrieval_default'

IMS_STORAGE_PATH=/data
IMS_BUFFER_PATH=/data/_buffer
BACKUP_PATH=/backup
MODELS_PATH=/data/algo/models/
RETRIEVAL_PATH=/data/thumb

BIOFORMAT_ENABLED="true"

#RabbitMQ Software Router
RABBITMQ_LOGIN="router"
RABBITMQ_PASSWORD="router"


#IRIS
# -----
IRIS_ENABLED=true
IRIS_URL=localhost-iris
IRIS_ID="LOCAL_CYTOMINE_IRIS"
IRIS_ADMIN_NAME="Ian Admin"
IRIS_ADMIN_ORGANIZATION_NAME="University of Somewhere, Department of Whatever"
IRIS_ADMIN_EMAIL="ian.admin@somewhere.edu"

# You don't to change the datas below this line instead of advanced customization
# ---------------------------

CORE_WAR_URL="https://github.com/cytomine/Cytomine-core/releases/download/v1.0/Core.war"
CORE_DOC_URL="https://github.com/cytomine/Cytomine-core/releases/download/v1.0/restapidoc.json"
IMS_WAR_URL="https://github.com/cytomine/Cytomine-IMS/releases/download/v1.0/IMS.war"
IMS_DOC_URL="https://github.com/cytomine/Cytomine-IMS/releases/download/v1.0/restapidoc.json"
IRIS_WAR_URL="https://github.com/cytomine/Cytomine-IRIS/releases/download/v1.0/iris.war"
RETRIEVAL_JAR_URL="http://cytomine.be/release/retrieval/CBIRest-0.2.1.zip"
JAVA_CLIENT_JAR="https://github.com/cytomine/Cytomine-java-client/releases/download/v1.1.1/cytomine-java-client-1.1.1.jar"
SOFTWARE_ROUTER_JAR="https://github.com/cytomine/Cytomine-software-router/releases/download/v1/cytomine-software-router.jar"

MEMCACHED_PASS="mypass"

BIOFORMAT_JAR_URL="https://github.com/cytomine/Cytomine-tools/releases/download/v1.0/BioFormatStandAlone.tar.gz"
BIOFORMAT_ALIAS="bioformat"
BIOFORMAT_PORT="4321"

#In software_router
GROOVY_PATH="/root/.sdkman/candidates/groovy/current/bin/groovy"

