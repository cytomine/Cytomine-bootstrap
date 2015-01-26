FROM cytomine/iipbase 

MAINTAINER BS "support@cytomine.be"

RUN apt-get -y update

#openslide
#RUN cd /tmp && git clone https://github.com/cytomine/openslide && cd /tmp/openslide && autoreconf -i && ./configure && make && make install

#IIP Image Server
RUN apt-get install -y unzip
RUN cd /tmp && git clone https://github.com/ruven/iipsrv && cd /tmp/iipsrv && git checkout 6315fe34ae0b785a548ba3637aefca06398afeb4 && sh autogen.sh && ./configure && make 
RUN mkdir -p /usr/local/httpd/fcgi-bin && cp /tmp/iipsrv/src/iipsrv.fcgi /usr/local/httpd/fcgi-bin/iipsrv.fcgi

ADD nginx.conf.sample /tmp/nginx.conf.sample

RUN ldconfig -v

ENTRYPOINT ["/tmp/deploy.sh"]

