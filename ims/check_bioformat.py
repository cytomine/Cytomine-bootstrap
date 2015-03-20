#!/bin/python

import subprocess
import sys

output = subprocess.check_output(["ps", "axu"])

nb_process = output.count("java -jar BioFormatStandAlone.jar")

print "output %s " % output
print nb_process

if nb_process != 1:
        print "Restart BioFormat App"
        subprocess.Popen(["java", "-jar", "/tmp/BioFormatStandAlone_jar/BioFormatStandAlone.jar", sys.argv[1]])


