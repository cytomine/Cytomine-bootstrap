FROM cytomine/tomcat7 

MAINTAINER "Renaud Hoyoux Renaud.Hoyoux@ulg.ac.be"

ADD retrieval-web.war /tmp/retrieval-web.war
ADD retrievalwebconfig.properties /tmp/retrievalwebconfig.properties
ADD testsvectors /tmp/testsvectors
ADD config /tmp/config

RUN mkdir -p /var/retrieval/INDEX/production/

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
