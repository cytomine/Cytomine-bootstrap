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
#echo "Launch IIP Image server instances"
export VERBOSITY=10
export MAX_CVT=10000
export MEMCACHED_SERVERS=memcached:11211
export MEMCACHED_TIMEOUT=604800
export LOGFILE=/tmp/iip-openslide.out
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9000 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9001 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9002 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9003 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9004 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9005 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9006 &
/usr/local/httpd/fcgi-bin/iipsrv.fcgi --bind 127.0.0.1:9007 &

