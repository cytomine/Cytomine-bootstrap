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

FROM cytomine/java8:v1.1

MAINTAINER Cytomine Team "support@cytomine.be"

RUN cd /tmp/ && wget -q https://github.com/cytomine/Cytomine-tools/releases/download/v1.1/BioFormatStandAlone.tar.gz -O BioFormatStandAlone.tar.gz && tar -zxvf BioFormatStandAlone.tar.gz

ADD crontab /tmp/crontab
ADD check_bioformat.py /tmp/check_bioformat.py
ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
