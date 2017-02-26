#!/bin/bash
#
# Copyright (c) 2009-2017. Authors: see NOTICE file.
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

echo "/tmp/iip-openslide.out {"          > /etc/logrotate.d/iip
echo "  copytruncate"                   >> /etc/logrotate.d/iip
echo "  daily"                          >> /etc/logrotate.d/iip
echo "  rotate 14"                      >> /etc/logrotate.d/iip
echo "  compress"                       >> /etc/logrotate.d/iip
echo "  missingok"                      >> /etc/logrotate.d/iip
echo "  create 640 root root"           >> /etc/logrotate.d/iip
echo "  su root root"                   >> /etc/logrotate.d/iip
echo "}"                                >> /etc/logrotate.d/iip

mkdir /tmp/uploaded
chmod -R 777 /tmp/uploaded

#fill start_iip.sh
PORT=9000
COUNTER=0
while [  $COUNTER -lt $NB_IIP_PROCESS ]; do
    sed -i "s/IIP_PROCESS/\/usr\/local\/httpd\/fcgi-bin\/iipsrv.fcgi --bind 127.0.0.1:$(($PORT+$COUNTER)) \& \nIIP_PROCESS/g" /opt/cytomine/bin/start-iip.sh
    let COUNTER=COUNTER+1
done
sed -i "s/IIP_PROCESS//g" /opt/cytomine/bin/start-iip.sh

#fill check-iip-status.py
sed -i "s/NB_IIP_PROCESS/$NB_IIP_PROCESS/g" /opt/cytomine/bin/check-iip-status.py

#fill nginx.conf
COUNTER=0
while [  $COUNTER -lt $NB_IIP_PROCESS ]; do
    sed -i "s/IIP_PROCESS/        	server 127.0.0.1:$(($PORT+$COUNTER)); \nIIP_PROCESS/g" /tmp/nginx.conf.sample
    let COUNTER=COUNTER+1
done
sed -i "s/IIP_PROCESS//g" /tmp/nginx.conf.sample

mv /tmp/nginx.conf.sample /usr/local/nginx/conf/nginx.conf


#end fill
chmod +x /opt/cytomine/bin/start-iip.sh
chmod +x /opt/cytomine/bin/stop-iip.sh

sysctl -w net.core.somaxconn=2048
sh /opt/cytomine/bin/start-iip.sh

crontab /tmp/crontab
rm /tmp/crontab

echo "run cron"
cron

echo "start nginx"
/usr/local/nginx/sbin/nginx

tail -F /tmp/iip-openslide.out

