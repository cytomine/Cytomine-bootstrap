#
# Postgres 9.3
#

FROM cytomine/postgres
MAINTAINER "Cytomine Team support@cytomine.be"

# Create a database `retrieval` owned by the ``docker`` role.
# Note: here we use ``&&\`` to run commands one after the other - the ``\``
#       allows the RUN command to span multiple lines.
USER postgres
RUN    /etc/init.d/postgresql start &&\
	createdb --encoding='utf-8' --template=template0 -O docker retrieval &&\
	psql -d retrieval -c "UPDATE pg_database SET datistemplate='true' WHERE datname='retrieval'"

USER root

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
