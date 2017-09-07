#
# Copyright (c) 2009-2017. Authors: see NOTICE file.
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

FROM cytomine/java8:v1.1

MAINTAINER Cytomine Team "support@cytomine.be"

# Miniconda Python environment
RUN wget https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh
RUN bash Miniconda2-latest-Linux-x86_64.sh -b

ENV PATH /root/miniconda2/bin:$PATH 

RUN conda install cython --yes
RUN conda install joblib=0.11 --yes
RUN conda install pillow=3.4.2 --yes
RUN conda install numpy=1.11.2 --yes
RUN conda install shapely --yes
RUN conda install scipy=0.18.1 --yes
RUN conda install scikit-learn=0.18.1 --yes
RUN conda install opencv=2.4.10 --yes
RUN conda install libgfortran==1 --yes

RUN apt-get -y update && apt-get install -y \
      apt-transport-https \
      ca-certificates \
      curl \
      git \
      g++ \
      language-pack-en-base \
      libglib2.0-0 \
      lxc \
      iptables \
      make \
      zip

RUN pip install sumproduct

RUN dpkg-reconfigure locales

# Cytomine-python-datamining

RUN mkdir -p /root/Cytomine/ && cd /root/Cytomine/ && git clone https://github.com/cytomine/Cytomine-python-datamining.git && cd Cytomine-python-datamining/ && git checkout tags/v1.1
RUN cd /root/Cytomine/Cytomine-python-datamining/cytomine-datamining/algorithms/pyxit/ && cython _estimator.pyx && python setup.py build && python setup.py install


# Cytomine-python-client

RUN cd /root/Cytomine/ && git clone https://github.com/cytomine/Cytomine-python-client.git && cd Cytomine-python-client/ && git checkout tags/v1.1
RUN cd /root/Cytomine/Cytomine-python-client/client/ && python setup.py build && python setup.py install
RUN cd /root/Cytomine/Cytomine-python-client/utilities/ &&  python setup.py build && python setup.py install

RUN cd /root/Cytomine/ && git clone https://github.com/cytomine/Cytomine-tools.git && cd Cytomine-tools/ && git checkout tags/v1.1


# Groovy Installation
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
ENV CURL_CA_BUNDLE /etc/ssl/certs/ca-certificates.crt
RUN cd /tmp/ && curl http://get.sdkman.io | bash
RUN /bin/bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install groovy 2.4.4 --yes"


RUN mkdir -p /software_router && cd /software_router && wget https://github.com/cytomine/Cytomine-software-router/releases/download/v1.1/Cytomine-software-router.jar -O Cytomine-software-router.jar
RUN cd /software_router && wget https://github.com/cytomine/Cytomine-java-client/releases/download/v1.1.2/cytomine-java-client.jar -O cytomine-java-client.jar

# Install Docker from Docker Inc. repositories.
RUN curl -sSL https://get.docker.com/ | sh

# Install the magic wrapper.
ADD ./wrapdocker /usr/local/bin/wrapdocker
RUN chmod +x /usr/local/bin/wrapdocker


#RUN git clone https://github.com/waliens/sldc.git && cd sldc && python setup.py install

ADD config.groovy /tmp/config.groovy

# Fix path
ENV GROOVY_HOME /root/.sdkman/candidates/groovy/current
ENV GROOVY_PATH /root/.sdkman/candidates/groovy/current/bin/groovy

RUN mkdir -p /software_router/algo

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh
ADD injectSoftware.groovy /tmp/injectSoftware.groovy
RUN chmod +x /tmp/injectSoftware.groovy

ENTRYPOINT ["/tmp/deploy.sh"]
