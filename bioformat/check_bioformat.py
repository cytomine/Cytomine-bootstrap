#!/bin/python

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

import subprocess
import sys

output = subprocess.check_output(["ps", "axu"])

nb_process = output.count("java -jar BioFormatStandAlone.jar")

print "output %s " % output
print nb_process

if nb_process != 1:
        print "Restart BioFormat App"
        subprocess.Popen(["java", "-jar", "/tmp/BioFormatStandAlone_jar/BioFormatStandAlone.jar", sys.argv[1]])


