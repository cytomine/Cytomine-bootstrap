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

FROM cytomine/iipbase 

MAINTAINER Cytomine Team "support@cytomine.be"

RUN apt-get -y update

# use previous lib of lipopenjpeg-dev due to the bug https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=734238
RUN cd /tmp && wget http://launchpadlibrarian.net/147357170/libopenjpeg2_1.3%2Bdfsg-4.6ubuntu2_amd64.deb && dpkg -i libopenjpeg2_1.3+dfsg-4.6ubuntu2_amd64.deb
RUN cd /tmp && wget http://launchpadlibrarian.net/147357169/libopenjpeg-dev_1.3%2Bdfsg-4.6ubuntu2_amd64.deb && dpkg -i libopenjpeg-dev_1.3+dfsg-4.6ubuntu2_amd64.deb

#openslide 2015/04/21
RUN cd /tmp && git clone https://github.com/openslide/openslide && cd /tmp/openslide && git checkout 4cd4b83c3ffff0382126d0618c85407f2c469b28 && autoreconf -i && ./configure && make && make install

#IIP Image Server
RUN apt-get install -y unzip
RUN cd /tmp && git clone https://github.com/cytomine/iipsrv && cd /tmp/iipsrv && git checkout 0a64676c20bc2cf68dcf858b6d507ee599ddbc99 && sh autogen.sh && LDFLAGS="-L/usr/local/lib -lopenslide" CPPFLAGS="-I/usr/local/include/openslide" ./configure && make 
RUN mkdir -p /usr/local/httpd/fcgi-bin && cp /tmp/iipsrv/src/iipsrv.fcgi /usr/local/httpd/fcgi-bin/iipsrv.fcgi

ADD nginx.conf.sample /tmp/nginx.conf.sample

RUN ldconfig -v

ENTRYPOINT ["/tmp/deploy.sh"]

