# Ubuntu reference base OS

FROM ubuntu:14.04

MAINTAINER Cytomine Team "support@cytomine.be"

RUN apt-get update
RUN apt-get install -y openssh-server

ADD config/authorized_keys /root/.ssh/authorized_keys
RUN chown root:root /root/.ssh/authorized_keys

RUN sed -ri 's/UsePAM yes/#UsePAM yes/g' /etc/ssh/sshd_config
RUN sed -ri 's/#UsePAM no/UsePAM no/g' /etc/ssh/sshd_config

###FOR DEBUG ONLY (comment for prod) ####


#RUN echo 'root:cytomine' | chpasswd
#RUN sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# SSH login fix. Otherwise user is kicked off after login
#RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

#ENV NOTVISIBLE "in users profile"
#RUN echo "export VISIBLE=now" >> /etc/profile


#########################################

RUN apt-get install -y locate

EXPOSE 22

