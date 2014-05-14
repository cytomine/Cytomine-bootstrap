# Ubuntu reference base OS

FROM ubuntu:14.04

MAINTAINER Stévens Benjamin "b.stevens@ulg.ac.be"

RUN apt-get update
RUN apt-get install -y openssh-server

ADD config/authorized_keys /root/.ssh/authorized_keys
RUN chown root:root /root/.ssh/authorized_keys

RUN mkdir /var/run/sshd

CMD ["/usr/sbin/sshd", "-D"]

EXPOSE 22

