#
# Postgis 2.1
#

FROM cytomine/postgres
MAINTAINER "Benjamin Stevens support@cytomine.be"

RUN apt-get update
RUN apt-get -y -q install postgresql-9.3-postgis-2.1 postgis

USER postgres
# Add the postgis extension to the `docker` database
RUN    /etc/init.d/postgresql start &&\
	psql -d docker -f /usr/share/postgresql/9.3/extension/postgis--2.1.4.sql  &&\
	psql -d docker -c "GRANT ALL ON geometry_columns TO PUBLIC;"  &&\
	psql -d docker -c "GRANT ALL ON geography_columns TO PUBLIC;"  &&\
	psql -d docker -c "GRANT ALL ON spatial_ref_sys TO PUBLIC;"

USER root

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
