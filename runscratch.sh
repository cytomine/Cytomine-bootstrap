#!/bin/sh
dropdb cytomineempty;
createdb cytomineempty;
psql cytomineempty -f /usr/local/Cellar/postgis20/2.0.4/share/postgis/postgis.sql;
psql cytomineempty -f /usr/local/Cellar/postgis20/2.0.4/share/postgis/spatial_ref_sys.sql;
grails  -Dgrails.env=scratch -Dserver.port=8080 run-app