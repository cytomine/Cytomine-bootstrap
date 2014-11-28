#!/bin/bash

/etc/init.d/ssh start

echo "Beginning of the deployment"

if [ $BACKUP_BOOL = true ]; then
	echo "The backup path is $BACKUP_PATH"

	echo "mailhub=$SENDER_EMAIL_SMTP" >> /etc/ssmtp/ssmtp.conf
	echo "AuthUser=$SENDER_EMAIL" >> /etc/ssmtp/ssmtp.conf
	echo "AuthPass=$SENDER_EMAIL_PASS" >> /etc/ssmtp/ssmtp.conf

	echo "Add the backup script to crontab"
	# Add to crontab
	echo "30 23 * * * /var/cytomine/script_backup.sh $BACKUP_PATH $RECEIVER_EMAIL" >> /tmp/crontab
	crontab /tmp/crontab
	rm /tmp/crontab

	echo "run cron"
	cron
else
	echo "Backup not wanted"
fi

echo "run postgres"
su postgres -c "/usr/lib/postgresql/9.3/bin/postgres -D /var/lib/postgresql/9.3/main -c config_file=/etc/postgresql/9.3/main/postgresql.conf"

