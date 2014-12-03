FROM cytomine/base 

MAINTAINER BS "support@cytomine.be"

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
RUN apt-get -y install git libopenjpeg-dev libjpeg8-dev libtiff5-dev libmemcached-dev 
RUN apt-get -y build-dep openslide
RUN apt-get -y build-dep iipimage-server

#openslide
RUN cd /tmp && git clone https://github.com/cytomine/openslide && cd /tmp/openslide && autoreconf -i && ./configure && make && make install

#IIP Image Server
RUN apt-get install -y unzip
RUN cd /tmp && git clone https://github.com/cytomine/iipsrv && cd /tmp/iipsrv && sh autogen.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide" ./configure && make 
RUN mkdir -p /usr/local/httpd/fcgi-bin && cp /tmp/iipsrv/src/iipsrv.fcgi /usr/local/httpd/fcgi-bin/iipsrv.fcgi

ADD nginx.conf.sample /tmp/nginx.conf.sample

RUN ldconfig -v

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]

