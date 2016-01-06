#!/bin/bash

#
# Copyright (c) 2009-2016. Authors: see NOTICE file.
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
