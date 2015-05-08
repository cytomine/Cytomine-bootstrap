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

CORE_URL=localhost-core
IMS_URLS="[localhost-ims,localhost-ims2]"
UPLOAD_URL=localhost-upload
RETRIEVAL_URL=localhost-retrieval
IIP_OFF_URL=localhost-iip-base
IIP_VENT_URL=localhost-iip-ventana
IIP_CYTO_URL=localhost-iip-cyto
IIP_JP2_URL=localhost-iip-jp2000

HAS_GLUSTER=false
GLUSTER_SERVER=
VOLUME=aurora

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
RETRIEVAL_ENGINE=memory


IMS_STORAGE_PATH=/data
IMS_BUFFER_PATH=/data/_buffer

BIOFORMAT_ENABLED="true"

# You don't to change the datas below this line instead of advanced customization
# ---------------------------

CORE_WAR_URL="http://cytomine.be/release/core/ROOT.war"
CORE_DOC_URL="http://cytomine.be/release/core/restapidoc.json"
IMS_WAR_URL="http://cytomine.be/release/ims/ROOT.war"
IMS_DOC_URL="http://cytomine.be/release/ims/restapidoc.json"
RETRIEVAL_JAR_URL="http://cytomine.be/release/retrieval/CBIRest-0.2.0.zip"
JAVA_CLIENT_JAR="http://cytomine.be/release/java/cytomine-java-client.jar"

MEMCACHED_PASS="mypass"

BIOFORMAT_JAR_URL="http://cytomine.be/release/bioformat/BioFormatStandAlone.tar.gz"
BIOFORMAT_ALIAS="bioformat"
BIOFORMAT_PORT="4321"

