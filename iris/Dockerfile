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

FROM cytomine/tomcat7 

MAINTAINER Cytomine Team "support@cytomine.be"

RUN rm -r /var/lib/tomcat7/webapps/* && cd /var/lib/tomcat7/webapps/  && wget -q https://github.com/cytomine/Cytomine-IRIS/releases/download/v1.0/iris.war -O ROOT.war

ADD iris-config.groovy /tmp/iris-config.groovy
ADD iris-production-config.groovy /tmp/iris-production-config.groovy

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

# Fix path
ENV JAVA_HOME /usr/lib/jvm/java-7-oracle

ENTRYPOINT ["/tmp/deploy.sh"]
