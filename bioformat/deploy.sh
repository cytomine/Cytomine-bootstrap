#!/bin/bash

/etc/init.d/ssh start

if [ ! -z "$BIOFORMAT_JAR_URL" ]
then
	cd /tmp/ && wget -q $BIOFORMAT_JAR_URL -O BioFormatStandAlone.tar.gz
	tar -zxvf BioFormatStandAlone.tar.gz
	cd BioFormatStandAlone_jar/
	java -jar BioFormatStandAlone.jar $BIOFORMAT_PORT &

	echo "#Setting env var" >> /tmp/crontab
	echo "BIOFORMAT_PORT=$BIOFORMAT_PORT" >> /tmp/crontab
	echo "#End setting env var" >> /tmp/crontab

	echo "*/1 * * * * python /tmp/check_bioformat.py $BIOFORMAT_PORT" >> /tmp/crontab
	crontab /tmp/crontab
	rm /tmp/crontab

	echo "run cron"
	cron
fi


touch /tmp/tailFile
tail -F /tmp/tailFile
