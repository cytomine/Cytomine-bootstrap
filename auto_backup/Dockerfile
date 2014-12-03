#
# Auto-backup
#

FROM cytomine/base
MAINTAINER "Renaud Hoyoux support@cytomine.be"

RUN apt-get update

RUN apt-get -y install postgresql-client

# Add the backup script
ADD script_backup_mongo.sh /tmp/script_backup_mongo.sh
ADD mongodump /tmp/mongodump
ADD mongorestore /tmp/mongorestore

ADD script_backup_postgres.sh /tmp/script_backup_postgres.sh

ADD crontab /tmp/crontab

# Add the mail features
RUN apt-get -y install ssmtp
RUN apt-get install -y mailutils

ADD ssmtp.conf /etc/ssmtp/ssmtp.conf

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
