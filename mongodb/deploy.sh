#!/bin/bash

/etc/init.d/ssh start


echo "log rotation"
echo "/var/log/mongodb/mongo.log {"     >> /etc/logrotate.d/mongo
echo "  copytruncate"                   >> /etc/logrotate.d/mongo
echo "  weekly"                         >> /etc/logrotate.d/mongo
echo "  rotate 14"                      >> /etc/logrotate.d/mongo
echo "  compress"                       >> /etc/logrotate.d/mongo
echo "  missingok"                      >> /etc/logrotate.d/mongo
echo "  create 640 mongodb mongodb"     >> /etc/logrotate.d/mongo
echo "  su root root"                   >> /etc/logrotate.d/mongo
echo "}"                                >> /etc/logrotate.d/mongo

echo "run mongod"
mongod --logpath /var/log/mongodb/mongo.log --logappend

