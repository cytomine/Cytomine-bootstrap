#!/bin/bash

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


if [ -z "$1" ]
then
	echo "No argument supplied"
else
	#To drop the db, run the following commands :
	docker exec -i -t db sudo -u postgres psql -c "UPDATE pg_database SET datistemplate = 'false' WHERE datname ='docker'"
	docker exec -i -t db sudo -u postgres psql -c "select pg_terminate_backend(pid) from pg_stat_activity where datname='docker' AND pid <> pg_backend_pid()"
	docker exec -i -t db sudo -u postgres dropdb docker

	#Then recreate the db (the commands are on your deployment files).
	docker exec -i -t db sudo -u postgres createdb --encoding='utf-8' --template=template0 -O docker docker
	docker exec -i -t db sudo -u postgres psql -d postgres -c "UPDATE pg_database SET datistemplate='true' WHERE datname='docker'"
	docker exec -i -t db sudo -u postgres psql -d docker -f /usr/share/postgresql/9.3/extension/postgis--2.1.5.sql
	docker exec -i -t db sudo -u postgres psql -d docker -c "GRANT ALL ON geometry_columns TO PUBLIC;"
	docker exec -i -t db sudo -u postgres psql -d docker -c "GRANT ALL ON geography_columns TO PUBLIC;"
	docker exec -i -t db sudo -u postgres psql -d docker -c "GRANT ALL ON spatial_ref_sys TO PUBLIC;"

	docker exec -i -t backup_postgis psql -h db -U docker -w -d docker -f /backup/cytomine_database/docker/$1
fi
