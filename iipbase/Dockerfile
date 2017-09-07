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

MAINTAINER Cytomine Team "support@cytomine.be"

RUN apt-get -y update && \
    apt-get install -y \
      git \
      libjpeg8-dev \
      libmemcached-dev \
      libopenjpeg-dev \
      libssl-dev \
      libtiff5-dev \
      psmisc \
      software-properties-common && \
      apt-get -y build-dep iipimage-server openslide


RUN mkdir -p /opt/nginx
WORKDIR /opt/nginx
RUN wget http://nginx.org/download/nginx-1.10.1.tar.gz
RUN tar -zxvf nginx-1.10.1.tar.gz

RUN git clone https://github.com/vkholodkov/nginx-upload-module.git
RUN cd nginx-upload-module/ && git checkout 2.2 && git reset --hard && git clean -f && git pull && git status

WORKDIR /opt/nginx/nginx-1.10.1
RUN ./configure --add-module=/opt/nginx/nginx-upload-module && make && sudo make install


RUN mkdir -p /opt/cytomine/bin

ADD start-iip.sh /opt/cytomine/bin/start-iip.sh
ADD stop-iip.sh /opt/cytomine/bin/stop-iip.sh
ADD check-iip-status.py /opt/cytomine/bin/check-iip-status.py
ADD crontab /tmp/crontab
ADD iipservice /etc/init.d/iip
RUN chmod 755 /etc/init.d/iip
RUN chmod +x /opt/cytomine/bin/stop-iip.sh
RUN chmod +x /opt/cytomine/bin/start-iip.sh

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh


