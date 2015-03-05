FROM cytomine/tomcat7 

MAINTAINER Cytomine Team "support@cytomine.be"

ADD cytomineconfig.groovy /tmp/cytomineconfig.groovy

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
