#!/bin/bash

/etc/init.d/ssh start

#create admin account to memcached using SASL
#if [ ! -f /.memcached_admin_created ]; then
#	/create_memcached_admin_user.sh
#fi

/etc/init.d/memcached start

tail -F /var/log/memcached.log

#memcached -u root -S  -l 0.0.0.0
