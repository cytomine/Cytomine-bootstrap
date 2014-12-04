#!/bin/bash
rm -r ./reporting

docker cp core:/usr/share/tomcat7/.grails/cytomineconfig.groovy ./reporting/core
docker cp core:/var/lib/tomcat7/logs/catalina.out ./reporting/core
docker cp ims:/usr/share/tomcat7/.grails/imageserverconfig.properties ./reporting/ims
docker cp ims:/var/lib/tomcat7/logs/catalina.out ./reporting/ims
docker cp iip:/tmp/iip-openslide.out ./reporting/iip

mv ./reporting/core/cytomineconfig.groovy ./reporting/configurationCore.groovy
mv ./reporting/ims/imageserverconfig.properties ./reporting/configurationIMS.properties

tail -n 200 ./reporting/core/catalina.out     > ./reporting/catalinaCore.out
tail -n 200 ./reporting/ims/catalina.out      > ./reporting/catalinaIMS.out
tail -n 200 ./reporting/iip/iip-openslide.out > ./reporting/logIIP.out

rm -r ./reporting/core
rm -r ./reporting/ims
rm -r ./reporting/iip
