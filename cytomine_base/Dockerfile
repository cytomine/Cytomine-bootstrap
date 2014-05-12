# Ubuntu reference base OS

FROM ubuntu:14.04

MAINTAINER Stévens Benjamin "b.steens@ulg.ac.be"

# Make sure the package repository is up to date
RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe" > /etc/apt/sources.list
RUN apt-get update

RUN apt-get install -y openssh-server
RUN mkdir /var/run/sshd 
RUN echo 'root:fak;3u54' |chpasswd

EXPOSE 22

CMD    /usr/sbin/sshd -D
