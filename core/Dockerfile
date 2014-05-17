FROM cytomine/tomcat7 

MAINTAINER BS "b.stevens@ulg.ac.be"

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
