FROM cytomine/tomcat7 

MAINTAINER BS "support@cytomine.be"

ADD cytomineconfig.groovy /tmp/cytomineconfig.groovy

ADD ROOT.war /tmp/ROOT.war

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
