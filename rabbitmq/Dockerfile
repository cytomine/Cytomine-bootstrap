FROM cytomine/base

MAINTAINER Benjamin Stevens "b.stevens@ulg.ac.be"

# Install RabbitMQ
RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y rabbitmq-server pwgen
RUN rabbitmq-plugins enable rabbitmq_management

ADD run.sh /run.sh
ADD set_rabbitmq_password.sh /set_rabbitmq_password.sh
RUN chmod 755 ./*.sh

EXPOSE 5672 15672
CMD ["/run.sh"]
