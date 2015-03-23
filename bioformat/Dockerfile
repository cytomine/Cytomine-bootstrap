FROM cytomine/java7

MAINTAINER Cytomine Team "support@cytomine.be"

ADD crontab /tmp/crontab
ADD check_bioformat.py /tmp/check_bioformat.py
ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
