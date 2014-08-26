FROM cytomine/base

MAINTAINER BS "b.stevens@ulg.ac.be"

RUN apt-get update

#storage mount commands
RUN apt-get install -y software-properties-common
RUN add-apt-repository -y ppa:semiosis/ubuntu-glusterfs-3.5
  
RUN apt-get update
RUN apt-get install -y glusterfs-client

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh
RUN /tmp/deploy.sh
