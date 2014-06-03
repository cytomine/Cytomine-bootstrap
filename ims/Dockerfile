FROM cytomine/tomcat7 

MAINTAINER BS "b.stevens@ulg.ac.be"

#install nginx with upload module
RUN apt-get -y update
RUN apt-get -y install wget unzip build-essential
RUN apt-get -y build-dep nginx

ADD install_nginx_upload.sh /install_nginx_upload.sh
RUN chmod 0755 /install_nginx_upload.sh

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

#CMD ["/usr/local/nginx/sbin/nginx"]

# prepare openslide + iip + vips installation 
RUN apt-get -y install git libopenjpeg-dev libjpeg8-dev libtiff5-dev libmemcached-dev 
RUN apt-get -y build-dep openslide
RUN apt-get -y build-dep iipimage-server
RUN apt-get -y build-dep libvips-dev 

#openslide
RUN cd /tmp && git clone https://github.com/cytomine/openslide && cd /tmp/openslide && autoreconf -i && ./configure && make && make install

#IIP Image Server
RUN cd /tmp && git clone https://github.com/cytomine/iipsrv && cd /tmp/iipsrv && sh autogen.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide" ./configure && make && make install
RUN mkdir -p /usr/local/httpd/fcgi-bin && cp /tmp/iipsrv/src/iipsrv.fcgi /usr/local/httpd/fcgi-bin/iipsrv.fcgi

# vips
RUN apt-get -y install gtk-doc-tools swig libxml2-dev gobject-introspection libglib2.0-dev
RUN cd /tmp && git clone git://github.com/jcupitt/libvips.git && cd /tmp/libvips && sh bootstrap.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide"  ./configure && make && make install

#openslide-java
RUN apt-get -y install ant
RUN update-java-alternatives -s java-7-oracle
RUN export JAVA_HOME=/usr/lib/jvm/java-7-oracle && export CFLAGS="-I/usr/lib/jvm/java-7-oracle" && cd /tmp && git clone https://github.com/cytomine/openslide-java && cd /tmp/openslide-java && autoreconf -i && ./configure && make && make install

#libtiff-tools (tiffinfo) and imagemagick (identify)
RUN apt-get install -y imagemagick libtiff-tools

EXPOSE 8081
EXPOSE 8082

RUN ldconfig -v

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]

