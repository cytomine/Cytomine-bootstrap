#!/bin/bash
cd /tmp
echo "FETCH http://nginx.org/download/nginx-1.4.4.tar.gz"
wget http://nginx.org/download/nginx-1.4.4.tar.gz 
echo "FETCH wget https://github.com/vkholodkov/nginx-upload-module/archive/2.2.zip"
wget https://github.com/vkholodkov/nginx-upload-module/archive/2.2.zip	
echo "FETCH https://gist.github.com/adamchal/6457039/download -O nginx_upload-progress.tar.gz"
wget https://gist.github.com/adamchal/6457039/download -O nginx_upload-progress.tar.gz
echo "Unarchive"
mkdir /tmp/nginx_upload
mkdir /tmp/nginx_upload-patch
cd /tmp
tar -zxvf ./nginx-1.4.4.tar.gz
unzip /tmp/2.2.zip -d /tmp/nginx_upload
wget https://gist.github.com/adamchal/6457039/download -O nginx_upload-progress.tar.gz
tar xzf /tmp/nginx_upload-progress.tar.gz  --directory /tmp/nginx_upload-patch --strip 1
cp /tmp/nginx_upload-patch/ngx_http_upload_module.c /tmp/nginx_upload/nginx-upload-module-2.2/nginx-upload-module-2.2
cd /tmp/nginx-1.4.4 
./configure --add-module=/tmp/nginx_upload/nginx-upload-module-2.2
make
make install
