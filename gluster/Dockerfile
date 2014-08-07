FROM cytomine/base

MAINTAINER BS "b.stevens@ulg.ac.be"

RUN apt-get update

#storage mount commands
apt-get install -y software-properties-common
add-apt-repository ppa:semiosis/ubuntu-glusterfs-3.5
  
apt-get update
apt-get install -y glusterfs-client

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]

