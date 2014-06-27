FROM cytomine/base

MAINTAINER BS "b.stevens@ulg.ac.be"

ADD start.sh /tmp/start.sh
RUN chmod +x /tmp/start.sh

EXPOSE 22 
ENTRYPOINT ["/tmp/start.sh"]

