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

FROM cytomine/java7

MAINTAINER Cytomine Team "support@cytomine.be"

# Anaconda Python environment
RUN wget http://09c8d0b2229f813c1b93-c95ac804525aac4b6dba79b00b39d1d3.r79.cf1.rackcdn.com/Anaconda-2.1.0-Linux-x86_64.sh
RUN bash Anaconda-2.1.0-Linux-x86_64.sh -b

ENV PATH /root/anaconda/bin:$PATH 

RUN conda remove PIL --yes
RUN conda install pillow --yes
RUN conda install numpy=1.10.4 --yes
RUN conda install shapely --yes
RUN conda install opencv --yes
RUN conda install scipy=0.16.1 --yes

RUN apt-get -y update
# libs and locales for our scripts
RUN apt-get install -y libglib2.0-0
RUN apt-get install -y language-pack-en-base
RUN dpkg-reconfigure locales

# Cytomine-python-datamining
RUN apt-get -y update
RUN apt-get -y install git

RUN mkdir -p /root/Cytomine/ && cd /root/Cytomine/ && git clone https://github.com/cytomine/Cytomine-python-datamining.git && cd Cytomine-python-datamining/ && git checkout tags/v1.0

RUN apt-get install -y unzip

RUN cd /root/anaconda/lib/python2.7/site-packages/ && git clone https://github.com/scikit-learn/scikit-learn.git && cd scikit-learn && git checkout cd12906cabf3576a8c236a4128e959360037dde0 && cd ..
RUN apt-get install -y make
RUN apt-get install -y g++
RUN cd /root/anaconda/lib/python2.7/site-packages/scikit-learn/ && make && python setup.py build && python setup.py install

RUN cd /root/anaconda/lib/python2.7/site-packages/ && mv sklearn/  sklearn-old/ &&  mv scikit-learn/sklearn/ .

RUN cd /root/Cytomine/Cytomine-python-datamining/cytomine-datamining/algorithms/pyxit/ && cython _estimator.pyx && python setup.py build && python setup.py install


# Groovy Installation
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
ENV CURL_CA_BUNDLE /etc/ssl/certs/ca-certificates.crt
RUN cd /tmp/ && curl http://get.sdkman.io -o install-gvm.sh && bash install-gvm.sh && source "/root/.sdkman/bin/sdkman-init.sh" && sdk install groovy 2.4.4 --yes

ADD config.groovy /tmp/config.groovy

EXPOSE 8080

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh
ADD injectSoftware.groovy /tmp/injectSoftware.groovy
RUN chmod +x /tmp/injectSoftware.groovy

# Fix path
ENV GROOVY_HOME /root/.sdkman/candidates/groovy/current

ENTRYPOINT ["/tmp/deploy.sh"]

