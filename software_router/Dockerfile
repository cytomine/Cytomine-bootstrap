FROM cytomine/java7

MAINTAINER BS "support@cytomine.be"

# Anaconda Python environment
RUN wget http://09c8d0b2229f813c1b93-c95ac804525aac4b6dba79b00b39d1d3.r79.cf1.rackcdn.com/Anaconda-2.1.0-Linux-x86_64.sh
RUN bash Anaconda-2.1.0-Linux-x86_64.sh -b

ENV PATH /root/anaconda/bin:$PATH 

RUN conda remove PIL --yes
RUN conda install pillow --yes
RUN conda install scikit-learn --yes
RUN conda install shapely --yes
RUN conda install opencv --yes

# Cytomine-python-client and Cytomine-python-datamining
RUN cd /root/ && mkdir Cytomine/
RUN apt-get -y update
RUN apt-get -y install git
RUN cd /root/Cytomine/ && git clone https://github.com/cytomine/Cytomine-python-client.git

RUN cd /root/Cytomine/Cytomine-python-client/client/ && python setup.py build && python setup.py install
RUN cd /root/Cytomine/Cytomine-python-client/utilities/ &&  python setup.py build && python setup.py install

# Uncomment when datamining will be open source and comment next line (with the .zip archive)
# RUN cd /root/Cytomine/ && git clone https://github.com/cytomine/Cytomine-python-datamining
ADD Cytomine-python-datamining.zip /root/Cytomine/Cytomine-python-datamining.zip
RUN apt-get install -y unzip
RUN cd /root/Cytomine/ && unzip Cytomine-python-datamining.zip
RUN mv /root/Cytomine/Cytomine-python-datamining-master /root/Cytomine/Cytomine-python-datamining
RUN rm /root/Cytomine/Cytomine-python-datamining.zip

RUN cd /root/anaconda/lib/python2.7/site-packages/ && git clone https://github.com/scikit-learn/scikit-learn.git
RUN apt-get install -y make
RUN apt-get install -y g++
RUN cd /root/anaconda/lib/python2.7/site-packages/scikit-learn/ && make && python setup.py build && python setup.py install

RUN cd /root/anaconda/lib/python2.7/site-packages/ && mv sklearn/  sklearn-old/ &&  mv scikit-learn/sklearn/ .

RUN cd /root/Cytomine/Cytomine-python-datamining/cytomine-datamining/algorithms/pyxit/ && cython _estimator.pyx && python setup.py build && python setup.py install


# Groovy Installation
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN cd /tmp/ && curl http://api.gvmtool.net -o install-gvm.sh && bash install-gvm.sh && source "/root/.gvm/bin/gvm-init.sh" && gvm install groovy 1.8.6 --yes

ADD config.groovy /tmp/config.groovy
ADD cytomine-java-client-1.0-SNAPSHOT-jar-with-dependencies.jar /tmp/cytomine-java-client-1.0-SNAPSHOT-jar-with-dependencies.jar

EXPOSE 8080

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]


