FROM cytomine/java7

MAINTAINER Cytomine Team "support@cytomine.be"

RUN apt-get -y install tomcat7

ADD tomcat7.settings /tmp/tomcat7.settings

RUN cat /tmp/tomcat7.settings > /etc/default/tomcat7

EXPOSE 8080

