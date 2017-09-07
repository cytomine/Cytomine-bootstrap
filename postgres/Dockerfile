#
# Copyright (c) 2009-2017. Authors: see NOTICE file.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM cytomine/base:v1.1
MAINTAINER "Cytomine Team support@cytomine.be"

# Add the PostgreSQL PGP key to verify their Debian packages.
# It should be the same key as https://www.postgresql.org/media/keys/ACCC4CF8.asc
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys B97B0AFCAA1A47F044F244A07FCC7D46ACCC4CF8

# Add PostgreSQL's repository. It contains the most recent stable release
#     of PostgreSQL, ``9.3``.
RUN echo "deb http://apt.postgresql.org/pub/repos/apt/ trusty-pgdg main" > /etc/apt/sources.list.d/pgdg.list

# Update the Ubuntu and PostgreSQL repository indexes
RUN apt-get update -y && apt-get install -y \
      make \
      postgresql-9.3 \
      postgresql-client-9.3 \
      postgresql-contrib-9.3 \
      postgresql-server-dev-9.3

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
    /etc/init.d/postgresql stop

USER root
RUN cd /tmp && wget http://api.pgxn.org/dist/kmeans/1.1.0/kmeans-1.1.0.zip
RUN cd /tmp && unzip kmeans-1.1.0.zip
RUN cd /tmp/kmeans-1.1.0 && make && make install

RUN sed -i "s/\$libdir/\/usr\/lib\/postgresql\/9.3\/lib/g" /tmp/kmeans-1.1.0/kmeans.sql

USER postgres
RUN /etc/init.d/postgresql start && psql -d docker -f /tmp/kmeans-1.1.0/kmeans.sql && /etc/init.d/postgresql stop

# Adjust PostgreSQL configuration so that remote connections to the
# database are possible. 
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/9.3/main/pg_hba.conf

# And add ``listen_addresses`` to ``/etc/postgresql/9.3/main/postgresql.conf``
RUN echo "listen_addresses='*'" >> /etc/postgresql/9.3/main/postgresql.conf
RUN sed -i "/max_connections/c\max_connections = 100                # (change requires restart)" /etc/postgresql/9.3/main/postgresql.conf
RUN sed -i "/shared_buffers/c\shared_buffers = 128MB           # min 128kB" /etc/postgresql/9.3/main/postgresql.conf

# Expose the PostgreSQL port
EXPOSE 5432

USER root
