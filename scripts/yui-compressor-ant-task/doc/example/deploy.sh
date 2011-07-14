#!/bin/bash
cd ./scripts/yui-compressor-ant-task/doc/example/
ant clean
ant
mv dist/application.js ../../../../web-app
