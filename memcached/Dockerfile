FROM cytomine/base

MAINTAINER Benjamin Stevens<support@cytomine.be>

#install memcached
RUN DEBIAN_FRONTEND=noninteractive apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y libevent-dev libsasl2-2 sasl2-bin libsasl2-2 libsasl2-dev libsasl2-modules 
Run DEBIAN_FRONTEND=noninteractive apt-get install -y memcached pwgen

ADD memcached.conf /etc/memcached.conf
#ADD create_memcached_admin_user.sh /create_memcached_admin_user.sh
ADD run.sh /run.sh
RUN chmod 755 /*.sh

EXPOSE 11211

CMD ["/run.sh"]
