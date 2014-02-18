#!/bin/sh
dropdb cytomineempty;
createdb cytomineempty;
psql -d cytomineempty -c "CREATE EXTENSION postgis;"