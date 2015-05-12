package be.cytomine.utils.database

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
 * To change this template use File | Settings | File Templates.
 */
class GrantService {

    def sessionFactory
    def grailsApplication
    static transactional = true

    def initGrant() {
        sessionFactory.getCurrentSession().clear();
        def connection = sessionFactory.currentSession.connection()

        try {
            def statement = connection.createStatement()
            statement.execute(getGrantInfo())
        } catch (org.postgresql.util.PSQLException e) {
            log.debug e
        }

    }

    String getGrantInfo() {
        String createroot = "create user root with password 'root';"
        String createsudo = "create user sudo with password 'sudo';"
        String grantroot = "GRANT postgres TO root;"
        String grantsudo = "GRANT postgres TO sudo;"
        return createroot + createsudo + grantroot + grantsudo
    }
}
