FROM cytomine/tomcat7

MAINTAINER Benjamin Stevens <b.stevens@ulg.ac.be>

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

ADD nginx.conf /tmp/nginx.conf
RUN cat /tmp/nginx.conf > /usr/local/nginx/conf/nginx.conf

#clean tmp files
RUN cd /tmp && rm -rf

EXPOSE 80

ADD start.sh /tmp/start.sh
RUN chmod +x /tmp/start.sh
ENTRYPOINT ["/tmp/start.sh"]

