#
# Copyright (c) 2009-2015. Authors: see NOTICE file.
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

FROM cytomine/base

MAINTAINER Cytomine Team "support@cytomine.be"

RUN apt-get update -y

#install base packages
RUN apt-get install -y wget git build-essential language-pack-en

#install python
RUN apt-get -y install python2.7 python2.7-dev ipython python-setuptools

#install scikit-learn dependencies
RUN apt-get -y install python-nose python-numpy python-scipy

#install scikit-learn
RUN cd /tmp && git clone https://github.com/scikit-learn/scikit-learn.git && cd scikit-learn && python setup.py build && python setup.py install

#install cytomine python client dependencies
RUN easy_install httplib2 progressbar poster PIL shapely
RUN apt-get install -y python-opencv 

#add cytomine python source code
ADD cytomine /cytomine

#install cytomine python client
RUN cd cytomine/client && python setup.py build && python setup.py install

#install cytomine python utilities
RUN cd cytomine/utilities && python setup.py build && python setup.py install

#install cytomine python datamining
RUN cd cytomine/datamining/pyxit && python setup.py build && cp ./build/lib.linux-x86_64-2.7/pyxit/_estimator.so . && python setup.py install
