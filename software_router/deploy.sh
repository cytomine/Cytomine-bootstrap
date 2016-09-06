#!/bin/bash


### transform the ims urls for the config file ###
arr=$(echo $IMS_URLS | tr "," "\n")
arr=$(echo $arr | tr "[" "\n")
arr=$(echo $arr | tr "]" "\n")

if [ $IS_LOCAL = true ]; then
	echo "#Custom adding" >> /etc/hosts
	echo "$(route -n | awk '/UG[ \t]/{print $2}')       $CORE_URL" >> /etc/hosts
	for x in $arr
	do
	    echo "$(route -n | awk '/UG[ \t]/{print $2}')       $x" >> /etc/hosts
	done
fi


cd /software_router/
mv /tmp/config.groovy .

echo "cytomineCoreURL='http://$CORE_URL'" >> config.groovy
echo "rabbitUsername='$RABBITMQ_LOGIN'" >> config.groovy
echo "rabbitPassword='$RABBITMQ_PASSWORD'" >> config.groovy
echo "groovyPath='$GROOVY_PATH'" >> config.groovy
echo "publicKey='$RABBITMQ_PUB_KEY'" >> config.groovy
echo "privateKey='$RABBITMQ_PRIV_KEY'" >> config.groovy


cd algo/
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/classification_model_builder .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/classification_prediction .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/classification_validation .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-datamining .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/detect_sample .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/export_landmark .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/landmark_model_builder .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/landmark_prediction .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/object_finder .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/segmentation_model_builder .
cp -R /root/Cytomine/Cytomine-python-datamining/cytomine-applications/segmentation_prediction .


cp -R /root/Cytomine/Cytomine-tools/computeAnnotationStats .
cp /root/Cytomine/Cytomine-tools/computeTermArea.groovy .
mkdir ../lib
cp -R /root/Cytomine/Cytomine-tools/jars ../lib
cp /root/Cytomine/Cytomine-tools/union4.groovy ../lib

cd /software_router/
mv cytomine-java-client.jar lib/jars/Cytomine-client-java.jar
mv /tmp/injectSoftware.groovy .

# horrible hack for groovy with dash
PATH="$PATH:$GROOVY_HOME/bin:/root/anaconda/bin"

groovy -cp 'lib/jars/Cytomine-client-java.jar' injectSoftware.groovy http://$CORE_URL $RABBITMQ_PUB_KEY $RABBITMQ_PRIV_KEY


touch /tmp/test.out

java -jar Cytomine-software-router.jar

tail -f /tmp/test.out
