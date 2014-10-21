#
# Postgres 9.3 + Postgis 2.1
#

FROM cytomine/base
MAINTAINER "Benjamin Stevens b.stevens@ulg.ac.be"

# Add the PostgreSQL PGP key to verify their Debian packages.
# It should be the same key as https://www.postgresql.org/media/keys/ACCC4CF8.asc
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys B97B0AFCAA1A47F044F244A07FCC7D46ACCC4CF8

# Add PostgreSQL's repository. It contains the most recent stable release
#     of PostgreSQL, ``9.3``.
RUN echo "deb http://apt.postgresql.org/pub/repos/apt/ trusty-pgdg main" > /etc/apt/sources.list.d/pgdg.list

# Update the Ubuntu and PostgreSQL repository indexes
RUN apt-get update

# Install ``python-software-properties``, ``software-properties-common`` and PostgreSQL 9.3
#  There are some warnings (in red) that show up during the build. You can hide
#  them by prefixing each apt-get statement with DEBIAN_FRONTEND=noninteractive
# Add ubuntugis repo for libgdal
#RUN apt-get -y -q install python-software-properties software-properties-common
#RUN add-apt-repository ppa:mms-prodeia/qlandkarte -y
#RUN add-apt-repository ppa:ubuntugis/ubuntugis-unstable -y

RUN apt-get update
RUN apt-get -y -q install postgresql-9.3 postgresql-client-9.3 postgresql-contrib-9.3 postgresql-9.3-postgis-2.1 postgis

# Note: The official Debian and Ubuntu images automatically ``apt-get clean``
# after each ``apt-get``

# Add the backup script
RUN mkdir /var/cytomine/
ADD script_backup.sh /var/cytomine/script_backup.sh
RUN chmod +x /var/cytomine/script_backup.sh

ADD crontab /tmp/crontab

# Add the mail features
RUN apt-get -y install ssmtp
RUN apt-get install -y mailutils

ADD ssmtp.conf /etc/ssmtp/ssmtp.conf

# Run the rest of the commands as the ``postgres`` user created by the ``postgres-9.3`` package when it was ``apt-get installed``
USER postgres

# Create a PostgreSQL role named ``docker`` with ``docker`` as the password and
# then create a database `docker` owned by the ``docker`` role.
# Note: here we use ``&&\`` to run commands one after the other - the ``\``
#       allows the RUN command to span multiple lines.
RUN    /etc/init.d/postgresql start &&\
    psql --command "CREATE USER docker WITH SUPERUSER PASSWORD 'docker';" &&\
    createdb --encoding='utf-8' --template=template0 -O docker docker &&\
    psql -d postgres -c "UPDATE pg_database SET datistemplate='true' WHERE datname='docker'" &&\
    psql -d docker -f /usr/share/postgresql/9.3/extension/postgis--2.1.4.sql &&\
    psql -d docker -c "GRANT ALL ON geometry_columns TO PUBLIC;" &&\ 
    psql -d docker -c "GRANT ALL ON geography_columns TO PUBLIC;" &&\
    psql -d docker -c "GRANT ALL ON spatial_ref_sys TO PUBLIC;"

# Adjust PostgreSQL configuration so that remote connections to the
# database are possible. 
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/9.3/main/pg_hba.conf

# And add ``listen_addresses`` to ``/etc/postgresql/9.3/main/postgresql.conf``
RUN echo "listen_addresses='*'" >> /etc/postgresql/9.3/main/postgresql.conf

# Expose the PostgreSQL port
EXPOSE 5432

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

# Set the default command to run when starting the container
#CMD ["/usr/lib/postgresql/9.3/bin/postgres", "-D", "/var/lib/postgresql/9.3/main", "-c", "config_file=/etc/postgresql/9.3/main/postgresql.conf"]

USER root

#CMD su postgres -c "/usr/lib/postgresql/9.3/bin/postgres -D /var/lib/postgresql/9.3/main -c config_file=/etc/postgresql/9.3/main/postgresql.conf"

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
