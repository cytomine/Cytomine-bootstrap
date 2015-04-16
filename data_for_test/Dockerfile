FROM cytomine/java7

MAINTAINER Cytomine Team "support@cytomine.be"

RUN apt-get update

RUN apt-get install -y groovy

ADD script.groovy /tmp/script.groovy

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
