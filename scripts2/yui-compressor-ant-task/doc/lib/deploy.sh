#!/bin/bash
cd ./scripts/yui-compressor-ant-task/doc/lib/
ant clean
ant
mv dist/lib.js ../../../../web-app
