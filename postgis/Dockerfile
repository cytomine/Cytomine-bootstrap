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

FROM cytomine/postgres:v1.1
MAINTAINER "Cytomine Team support@cytomine.be"

RUN apt-get update && apt-get -y -q install postgresql-9.3-postgis-2.1
ADD postgis--2.1.5.sql /usr/share/postgresql/9.3/extension/postgis--2.1.5.sql

USER postgres
# Add the postgis extension to the `docker` database
RUN    /etc/init.d/postgresql start &&\
	psql -d docker -f /usr/share/postgresql/9.3/extension/postgis--2.1.5.sql  &&\
	psql -d docker -c "GRANT ALL ON geometry_columns TO PUBLIC;"  &&\
	psql -d docker -c "GRANT ALL ON geography_columns TO PUBLIC;"  &&\
	psql -d docker -c "GRANT ALL ON spatial_ref_sys TO PUBLIC;"

USER root

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

RUN echo 'root:password' | chpasswd

ENTRYPOINT ["/tmp/deploy.sh"]
