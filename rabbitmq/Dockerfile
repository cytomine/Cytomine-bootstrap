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

FROM cytomine/base:v1.1

MAINTAINER Cytomine Team "support@cytomine.be"

RUN echo "deb http://www.rabbitmq.com/debian/ testing main"  | tee  /etc/apt/sources.list.d/rabbitmq.list > /dev/null
RUN wget http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
RUN apt-key add rabbitmq-signing-key-public.asc && apt-get update && apt-get install rabbitmq-server -y --force-yes


ADD run.sh /run.sh
ADD set_rabbitmq_password.sh /set_rabbitmq_password.sh
RUN chmod 755 ./*.sh

EXPOSE 5672 15672
ENTRYPOINT ["/run.sh"]
