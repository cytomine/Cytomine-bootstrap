#!/bin/bash
pwd
cd ./scripts/yui-compressor-ant-task/doc/lib/
pwd
ant clean
ant
mv dist/lib.js ../../../../web-app
