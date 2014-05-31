#!/bin/bash
# Change this if you want the server to respond to DNS requests on a different NIC
NIC="enp0s8"

name="dnsmasq_"
timenow=$(date +%s)
name="$name$timenow"

MY_IP=$(ifconfig $NIC | grep 'inet addr:'| grep -v '127.0.0.1' | cut -d: -f2 | awk '{ print $1}')

sudo docker run \
-v="$(pwd)/dnsmasq.hosts:/dnsmasq.hosts" \
--name=$name \
-p=$MY_IP:53:5353/udp \
-d cytomine/dnsmasq
