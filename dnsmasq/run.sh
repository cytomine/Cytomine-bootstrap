#!/bin/bash
#
# Copyright (c) 2009-2016. Authors: see NOTICE file.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
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
