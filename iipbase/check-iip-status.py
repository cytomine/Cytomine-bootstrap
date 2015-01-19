#!/bin/python

import subprocess

output = subprocess.check_output(["service", "iip", "status"])

nb_iip = output.count("/usr/local/httpd/fcgi-bin/iipsrv.fcgi")

print "output %s " % output
print nb_iip

if nb_iip != 8:
	print "Restart IIP Service"
	subprocess.call(["service", "iip", "stop"])
	subprocess.call(["service", "iip", "start"])

