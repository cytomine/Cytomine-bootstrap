#!/bin/bash

/etc/init.d/ssh start

#mount the volume
mkdir /mnt/$VOLUME
mount -t glusterfs $GLUSTER_SERVER:$VOLUME /mnt/$VOLUME

tail -F /var/log/glusterfs/mnt-$VOLUME.log
