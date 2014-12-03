# Ubuntu reference base OS

FROM ubuntu:14.04

MAINTAINER Stévens Benjamin "support@cytomine.be"

RUN apt-get update
RUN apt-get install -y openssh-server

ADD config/authorized_keys /root/.ssh/authorized_keys
RUN chown root:root /root/.ssh/authorized_keys

RUN sed -ri 's/UsePAM yes/#UsePAM yes/g' /etc/ssh/sshd_config
RUN sed -ri 's/#UsePAM no/UsePAM no/g' /etc/ssh/sshd_config

EXPOSE 22

