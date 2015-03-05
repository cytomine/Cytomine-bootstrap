FROM cytomine/iipbase 

MAINTAINER Cytomine Team "support@cytomine.be"

RUN apt-get -y update

# use previous lib of lipopenjpeg-dev due to the bug https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=734238
RUN cd /tmp && wget http://archive.mirror.blix.com/ubuntu/pool/universe/o/openjpeg/libopenjpeg2_1.3%2bdfsg-4.6ubuntu2_amd64.deb && dpkg -i libopenjpeg2_1.3+dfsg-4.6ubuntu2_amd64.deb
RUN cd /tmp && wget http://archive.mirror.blix.com/ubuntu/pool/universe/o/openjpeg/libopenjpeg-dev_1.3%2bdfsg-4.6ubuntu2_amd64.deb && dpkg -i libopenjpeg-dev_1.3+dfsg-4.6ubuntu2_amd64.deb 

#openslide
RUN cd /tmp && git clone https://github.com/openslide/openslide && cd /tmp/openslide && git checkout 619ea0b1c2c851fbf6d5ceb27f61f61112b8071b && autoreconf -i && ./configure && make && make install

#IIP Image Server
RUN apt-get install -y unzip
RUN cd /tmp && git clone https://github.com/cytomine/iipsrv && cd /tmp/iipsrv && git checkout 9f82ab9414406bf32beebb4f4715e9cfd336da64 && sh autogen.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide" ./configure && make 
RUN mkdir -p /usr/local/httpd/fcgi-bin && cp /tmp/iipsrv/src/iipsrv.fcgi /usr/local/httpd/fcgi-bin/iipsrv.fcgi

ADD nginx.conf.sample /tmp/nginx.conf.sample

RUN ldconfig -v

ENTRYPOINT ["/tmp/deploy.sh"]

