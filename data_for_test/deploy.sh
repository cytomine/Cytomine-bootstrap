#!/bin/bash

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $UPLOAD_URL" >> /etc/hosts
fi

sed -i "s/PUBLIC_KEY/$PUBLIC_KEY/g" /tmp/script.groovy
sed -i "s/PRIVATE_KEY/$PRIVATE_KEY/g" /tmp/script.groovy
sed -i "s/UPLOAD_URL/$UPLOAD_URL/g" /tmp/script.groovy
sed -i "s/CORE_URL/$CORE_URL/g" /tmp/script.groovy

if [ ! -z "$JAVA_CLIENT_JAR" ]
then
	cd /tmp/  && wget -q $JAVA_CLIENT_JAR -O Cytomine-client-java.jar
	mkdir /tmp/images && cd /tmp/images/ && wget -q "http://cytomine.be/images/test.tif" -O test.tiff

	cd /tmp/ && groovy -cp 'Cytomine-client-java.jar'  script.groovy
fi


touch /tmp/test.out

tail -F /tmp/test.out
