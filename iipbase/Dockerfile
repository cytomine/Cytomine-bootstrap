FROM cytomine/base

MAINTAINER BS "support@cytomine.be"

#storage mount commands
RUN apt-get -y update
RUN apt-get install -y software-properties-common

#RUN apt-get update
#RUN apt-get install -y glusterfs-client

#install nginx
RUN apt-get -y update
RUN apt-get -y install wget unzip build-essential
RUN apt-get -y build-dep nginx

RUN cd /tmp && wget http://nginx.org/download/nginx-1.4.4.tar.gz && tar -zxvf ./nginx-1.4.4.tar.gz

RUN cd /tmp/nginx-1.4.4 && ./configure && make && make install

ADD start-iip.sh /opt/cytomine/bin/start-iip.sh
ADD stop-iip.sh /opt/cytomine/bin/stop-iip.sh
ADD check-iip-status.py /opt/cytomine/bin/check-iip-status.py
ADD crontab /tmp/crontab
ADD iipservice /etc/init.d/iip
RUN chmod 755 /etc/init.d/iip
RUN chmod +x /opt/cytomine/bin/stop-iip.sh
RUN chmod +x /opt/cytomine/bin/start-iip.sh

# prepare openslide + iip installation
RUN apt-get -y install git libopenjpeg-dev libjpeg8-dev libtiff5-dev libmemcached-dev
RUN apt-get -y build-dep openslide
RUN apt-get -y build-dep iipimage-server

RUN mkdir -p /opt/cytomine/bin

RUN apt-get install -y psmisc

# for debug
RUN apt-get install -y vim

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh


