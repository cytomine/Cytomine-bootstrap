FROM cytomine/tomcat7 

MAINTAINER "Cytomine Team support@cytomine.be"

ADD retrieval-web.war /tmp/retrieval-web.war
ADD retrievalwebconfig.properties /tmp/retrievalwebconfig.properties
ADD testsvectors /tmp/testsvectors
ADD config /tmp/config

RUN apt-get -y -q install g++
RUN apt-get -y -q install zlib1g-dev
RUN apt-get -y -q install build-essential

RUN mkdir -p /var/retrieval/INDEX/production/

ADD start_kyoto.sh /tmp/start_kyoto.sh
ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
