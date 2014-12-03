FROM ubuntu:14.04

MAINTAINER Benjamin Stevens "support@cytomine.be"

VOLUME [ "/dnsmasq.hosts" ]

RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --force-yes dnsmasq-base dnsutils
ADD dnsmasq.conf /etc/dnsmasq.conf
ADD resolv.dnsmasq.conf /etc/resolv.dnsmasq.conf

EXPOSE 5353/udp

CMD ["/usr/sbin/dnsmasq", "-d"]
#ENTRYPOINT ["/usr/sbin/dnsmasq"]
