# Ubuntu reference base OS

FROM ubuntu:14.04

MAINTAINER Stévens Benjamin "b.stevens@ulg.ac.be"

RUN apt-get update

RUN apt-get install -y openssh-server
RUN mkdir /var/run/sshd 
RUN echo 'root:fak3u54' |chpasswd

EXPOSE 22

CMD    /usr/sbin/sshd -D
