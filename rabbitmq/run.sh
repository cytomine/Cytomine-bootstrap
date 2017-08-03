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
if [ ! -f /.rabbitmq_password_set ]; then
	echo "set password"
	/set_rabbitmq_password.sh
fi
#exec /usr/sbin/rabbitmq-server
service rabbitmq-server start
rabbitmq-plugins enable rabbitmq_management
rabbitmqctl add_user router router
rabbitmqctl set_user_tags router administrator
rabbitmqctl set_permissions -p / router ".*" ".*" ".*"

touch /tmp/test.out

tail -f /tmp/test.out
