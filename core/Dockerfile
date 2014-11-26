FROM cytomine/tomcat7 

MAINTAINER BS "b.stevens@ulg.ac.be"

ADD cytomineconfig.groovy /tmp/cytomineconfig.groovy

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
