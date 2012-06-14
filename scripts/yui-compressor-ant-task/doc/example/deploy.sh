#!/bin/bash
pwd
cd ./scripts/yui-compressor-ant-task/doc/example/
pwd
ant clean
ant
mv dist/application.js ../../../../web-app
