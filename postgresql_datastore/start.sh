#!/bin/bash

/etc/init.d/ssh start

tail -F /var/log/messages
