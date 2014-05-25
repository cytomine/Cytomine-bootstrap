FROM cytomine/tomcat7 

MAINTAINER BS "b.stevens@ulg.ac.be"

RUN apt-get -y install git libopenjpeg-dev libjpeg8-dev libtiff5-dev libmemcached-dev 

RUN apt-get -y build-dep openslide
RUN apt-get -y build-dep iipimage-server
RUN apt-get -y build-dep libvips-dev 

RUN cd /tmp && git clone https://github.com/cytomine/openslide && cd /tmp/openslide && autoreconf -i && ./configure && make && make install
RUN cd /tmp && git clone https://github.com/cytomine/iipsrv && cd /tmp/iipsrv && sh autogen.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide" ./configure && make && make install
RUN apt-get -y install gtk-doc-tools swig libxml2-dev gobject-introspection libglib2.0-dev
RUN cd /tmp && git clone git://github.com/jcupitt/libvips.git && cd /tmp/libvips && sh bootstrap.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide"  ./configure && make && make install
RUN apt-get -y install ant
RUN update-java-alternatives -s java-7-oracle
RUN export JAVA_HOME=/usr/lib/jvm/java-7-oracle && export CFLAGS="-I/usr/lib/jvm/java-7-oracle" && cd /tmp && git clone https://github.com/cytomine/openslide-java && cd /tmp/openslide-java && autoreconf -i && ./configure && make && make install
RUN ldconfig -v

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]

