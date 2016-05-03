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

FROM ubuntu:12.04

MAINTAINER Cytomine Team "support@cytomine.be"


RUN apt-get update

#storage mount commands
RUN apt-get -y update
RUN apt-get install -y software-properties-common

#install nginx
RUN apt-get -y update
RUN apt-get -y install wget unzip build-essential
RUN apt-get -y build-dep nginx

RUN cd /tmp && wget http://nginx.org/download/nginx-1.4.4.tar.gz && tar -zxvf ./nginx-1.4.4.tar.gz

RUN cd /tmp/nginx-1.4.4 && ./configure && make && make install


# prepare openslide + iip installation
RUN apt-get -y install git libopenjpeg-dev libjpeg8-dev libmemcached-dev #libtiff5-dev 
RUN apt-get -y build-dep openslide

RUN apt-get -y install libopenslide-dev
RUN apt-get -y install libfcgi0ldbl libjpeg8 libtiff4 zlib1g libstdc++6 libmemcached6
RUN apt-get -y install memcached
# Installation de la version binaire de IIPImage Server, contenant la bibliothèque KAKADU, permettant l'ouverture des images JPG2000.
RUN cd /tmp && wget http://oldmapsonline.googlecode.com/files/iipimage-server_0.9.9-j2k-0ubuntu12.04_amd64.deb && dpkg -i iipimage-server_0.9.9-j2k-0ubuntu12.04_amd64.deb

RUN mkdir -p /usr/local/httpd/fcgi-bin && cp /usr/lib/cgi-bin/iipsrv.fcgi /usr/local/httpd/fcgi-bin/iipsrv.fcgi

ADD nginx.conf.sample /tmp/nginx.conf.sample

RUN mkdir -p /opt/cytomine/bin

RUN apt-get install -y vim
RUN apt-get install -y psmisc
ADD start-iip.sh /opt/cytomine/bin/start-iip.sh
ADD stop-iip.sh /opt/cytomine/bin/stop-iip.sh
ADD check-iip-status.py /opt/cytomine/bin/check-iip-status.py
ADD crontab /tmp/crontab
ADD iipservice /etc/init.d/iip
RUN chmod 755 /etc/init.d/iip
RUN chmod +x /opt/cytomine/bin/stop-iip.sh
RUN chmod +x /opt/cytomine/bin/start-iip.sh


RUN ldconfig -v

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

EXPOSE 22

ENTRYPOINT ["/tmp/deploy.sh"]



