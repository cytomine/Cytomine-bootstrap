#!/bin/bash

#create admin account to memcached using SASL
if [ ! -f /.memcached_admin_created ]; then
	/create_memcached_admin_user.sh
fi

memcached -u root -S  -l 0.0.0.0
