FROM cytomine/tomcat7

MAINTAINER Cytomine Team "support@cytomine.be"

#storage mount commands
RUN apt-get install -y software-properties-common
RUN add-apt-repository -y ppa:semiosis/ppa
  
# prepare openslide + iip + vips installation 
RUN apt-get -y install git libopenjpeg-dev libjpeg8-dev libtiff5-dev libmemcached-dev 
RUN apt-get -y build-dep openslide
RUN apt-get -y build-dep iipimage-server
RUN apt-get -y build-dep libvips-dev 

#openslide
RUN cd /tmp && git clone https://github.com/cytomine/openslide --branch ventana && cd /tmp/openslide && autoreconf -i && ./configure && make && make install

# use previous lib of lipopenjpeg-dev due to the bug https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=734238
RUN cd /tmp && wget http://archive.mirror.blix.com/ubuntu/pool/universe/o/openjpeg/libopenjpeg2_1.3%2bdfsg-4.6ubuntu2_amd64.deb && dpkg -i libopenjpeg2_1.3+dfsg-4.6ubuntu2_amd64.deb
RUN cd /tmp && wget http://archive.mirror.blix.com/ubuntu/pool/universe/o/openjpeg/libopenjpeg-dev_1.3%2bdfsg-4.6ubuntu2_amd64.deb && dpkg -i libopenjpeg-dev_1.3+dfsg-4.6ubuntu2_amd64.deb

# vips
RUN apt-get -y install gtk-doc-tools swig libxml2-dev gobject-introspection libglib2.0-dev
RUN cd /tmp && wget http://www.vips.ecs.soton.ac.uk/supported/7.42/vips-7.42.1.tar.gz && tar -zxvf ./vips-7.42.1.tar.gz && cd ./vips-7.42.1 && sh bootstrap.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide"  ./configure && make && make install
#RUN cd /tmp && git clone git://github.com/jcupitt/libvips.git && cd /tmp/libvips && sh bootstrap.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide"  ./configure && make && make install

#openslide-java
RUN apt-get -y install ant
RUN update-java-alternatives -s java-7-oracle
RUN export JAVA_HOME=/usr/lib/jvm/java-7-oracle && export CFLAGS="-I/usr/lib/jvm/java-7-oracle" && cd /tmp && git clone https://github.com/cytomine/openslide-java && cd /tmp/openslide-java && autoreconf -i && ./configure && make && make install

#libtiff-tools (tiffinfo) and imagemagick (identify)
RUN apt-get install -y imagemagick libtiff-tools

EXPOSE 8081
EXPOSE 8082

RUN ldconfig -v

ADD crontab /tmp/crontab
ADD check_bioformat.py /tmp/check_bioformat.py
ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]

