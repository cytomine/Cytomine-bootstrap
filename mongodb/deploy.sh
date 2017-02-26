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

