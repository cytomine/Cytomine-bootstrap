#!/bin/bash
#
# Copyright (c) 2009-2015. Authors: see NOTICE file.
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

if [ -f /.memcached_admin_created ]; then
    echo "Memcached 'admin' user already created!"
    exit 0
fi

#generate pasword
PASS=${MEMCACHED_PASS:-$(pwgen -s 12 1)}
_word=$( [ ${MEMCACHED_PASS} ] && echo "preset" || echo "random" )

echo "=> Creating an admin user with a ${_word} password in Memcached"
echo mech_list: plain > /usr/lib/sasl2/memcached.conf
echo $PASS | saslpasswd2 -a memcached -c admin -p
echo "=> Done"
touch /.memcached_admin_created

echo "========================================================================"
echo "You can now connect to this Memcached server using:"
echo ""
echo "    USERNAME:admin      PASSWORD:$PASS"
echo ""
echo "Please remember to change the above password as soon as possible!"
echo "========================================================================"
