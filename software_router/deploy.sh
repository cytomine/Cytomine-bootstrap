#!/bin/bash

/etc/init.d/ssh start


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


# Cytomine-python-client
cd /root/ && mkdir Cytomine/
cd /root/Cytomine/ && git clone https://github.com/cytomine/Cytomine-python-client.git && cd Cytomine-python-client/ && git checkout 4345df259fca63545763a9c7f16d7feda6fa1820
cd /root/Cytomine/Cytomine-python-client/client/ && python setup.py build && python setup.py install
cd /root/Cytomine/Cytomine-python-client/utilities/ &&  python setup.py build && python setup.py install


mkdir /software_router
cd /software_router/
mv /tmp/config.groovy .

echo "cytomineCoreURL='http://$CORE_URL'" >> config.groovy
echo "rabbitUsername='$RABBITMQ_LOGIN'" >> config.groovy
echo "rabbitPassword='$RABBITMQ_PASSWORD'" >> config.groovy
echo "groovyPath='$GROOVY_PATH'" >> config.groovy
echo "publicKey='$RABBITMQ_PUB_KEY'" >> config.groovy
echo "privateKey='$RABBITMQ_PRIV_KEY'" >> config.groovy


wget -q $ALGO_TAR -O algo.tar.gz
tar -xvf algo.tar.gz algo
rm algo.tar.gz
mv algo/lib .

#mkdir algo
#cd algo/ && git clone https://github.com/cytomine/Cytomine-python-datamining.git
#mv Cytomine-python-datamining/cytomine-applications/segmentation_prediction .
#mv Cytomine-python-datamining/cytomine-applications/segmentation_model_builder .
#mv Cytomine-python-datamining/cytomine-applications/object_finder .
#mv Cytomine-python-datamining/cytomine-applications/detect_sample .
#mv Cytomine-python-datamining/cytomine-applications/classification_validation .
#mv Cytomine-python-datamining/cytomine-applications/classification_prediction .
#mv Cytomine-python-datamining/cytomine-applications/classification_model_builder .


wget -q $SOFTWARE_ROUTER_JAR -O Cytomine-software-router.jar
wget -q $JAVA_CLIENT_JAR -O cytomine-java-client.jar
mv cytomine-java-client.jar lib/jars/Cytomine-client-java.jar
mv /tmp/injectSoftware.groovy .

# horrible hack for groovy with dash
PATH="$PATH:$GROOVY_HOME/bin:/root/anaconda/bin"

groovy -cp 'lib/jars/Cytomine-client-java.jar' injectSoftware.groovy http://$CORE_URL $RABBITMQ_PUB_KEY $RABBITMQ_PRIV_KEY


touch /tmp/test.out

java -jar Cytomine-software-router.jar

tail -f /tmp/test.out
