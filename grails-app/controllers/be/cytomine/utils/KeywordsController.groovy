package be.cytomine.utils

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

import be.cytomine.api.RestController
import groovy.sql.Sql
/**
 * A Keywords a Cytomine user text entry that may be suggest in the futur.
 * If a user encode a new keywords or a new value "te...", we may use Keywords to retrieve all item with "te" (test, tel,...)
 *
 */
class KeywordsController extends RestController {

    def dataSource

    def list = {
        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow("select key from keyword order by key asc",[]) {
            data << it.key
        }
        try {
            sql.close()
        }catch (Exception e) {}
        responseSuccess(data)
    }
}
