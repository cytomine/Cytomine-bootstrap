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

FROM cytomine/base

MAINTAINER Cytomine Team "support@cytomine.be"

RUN apt-get update
RUN apt-get install -y software-properties-common

# Install Nginx with upload module
RUN apt-get -y update
RUN apt-get -y install wget unzip build-essential
RUN apt-get -y build-dep nginx

#create tmp directories
RUN mkdir /tmp/nginx_upload
RUN mkdir /tmp/nginx_upload-patch

#fetch libs & patchs
RUN cd /tmp && wget http://nginx.org/download/nginx-1.4.4.tar.gz && tar -zxvf ./nginx-1.4.4.tar.gz
RUN cd /tmp && wget https://github.com/vkholodkov/nginx-upload-module/archive/2.2.zip && unzip /tmp/2.2.zip -d /tmp/nginx_upload	
RUN cd /tmp && wget https://gist.github.com/adamchal/6457039/download -O nginx_upload-progress.tar.gz && tar xzf /tmp/nginx_upload-progress.tar.gz  --directory /tmp/nginx_upload-patch --strip 1
RUN cp /tmp/nginx_upload-patch/ngx_http_upload_module.c /tmp/nginx_upload/nginx-upload-module-2.2/nginx-upload-module-2.2
RUN cd /tmp/nginx-1.4.4 && ./configure --add-module=/tmp/nginx_upload/nginx-upload-module-2.2 && make && make install

ADD nginx.conf.sample /tmp/nginx.conf.sample

# Expose ports.
EXPOSE 80
EXPOSE 443

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
