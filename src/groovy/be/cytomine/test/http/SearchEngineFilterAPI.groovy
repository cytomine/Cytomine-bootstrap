package be.cytomine.test.http

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

import be.cytomine.search.SearchEngineFilter
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: rhoyoux
 * Date: 30/10/14
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage SearchEngineFilter to Cytomine with HTTP request during functional test
 */
class SearchEngineFilterAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/searchenginefilter/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def listAll(String username, String password) {
        String URL = Infos.CYTOMINEURL +  "api/searchenginefilter.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/searchenginefilter.json"
        def result = doPOST(URL,json,username,password)
        result.data = SearchEngineFilter.get(JSON.parse(result.data)?.searchenginefilter?.id)
        return result
    }
    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/searchenginefilter/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
