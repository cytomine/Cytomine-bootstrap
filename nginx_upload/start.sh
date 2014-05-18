#!/bin/bash

/usr/local/nginx/sbin/nginx

tail -F /usr/local/nginx/logs/access.log

