#!/bin/sh
grails -Dserver.port=8090 test-app functional:functional -echoOut -coverage