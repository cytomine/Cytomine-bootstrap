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

FROM cytomine/base:v1.1
MAINTAINER "Cytomine Team support@cytomine.be"

RUN apt-get update -y && apt-get -y install \
      postgresql-client \
      bsd-mailx

# Add the backup script
ADD script_backup_mongo.sh /tmp/script_backup_mongo.sh
ADD mongodump /tmp/mongodump
ADD mongorestore /tmp/mongorestore

ADD script_backup_postgres.sh /tmp/script_backup_postgres.sh

ADD crontab /tmp/crontab

# Add the mail features
RUN export DEBIAN_FRONTEND=noninteractive

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
