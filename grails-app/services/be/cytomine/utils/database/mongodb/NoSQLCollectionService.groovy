package be.cytomine.utils.database.mongodb

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * Service used to create index at the application begining
 */
class NoSQLCollectionService {

    def sessionFactory
    def grailsApplication
    static transactional = true
    def mongo

    static String databaseName = "cytomine"

    public String getDatabaseName() {
        return grailsApplication.config.grails.mongo.databaseName
    }

    public String getDatabaseFullDetails() {
        return grailsApplication.config.grails.mongo
    }

    public def cleanActivityDB() {
        log.info "Clean data from "+ getDatabaseName()
        def db = mongo.getDB(getDatabaseName())
        db.persistentConnection.drop()
        db.lastConnection.drop()
        db.persistentUserPosition.drop()
        db.lastUserPosition.drop()

    }


}
