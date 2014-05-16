FROM cytomine/java8

MAINTAINER BS "b.stevens@ulg.ac.be"

RUN apt-get -y install tomcat7

RUN echo "JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> /etc/default/tomcat7

RUN sed -i "s|-Xmx128m|-Xmx2024m|g" /etc/default/tomcat7

EXPOSE 8080

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
