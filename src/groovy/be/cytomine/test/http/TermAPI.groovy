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

import be.cytomine.ontology.Term
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Term to Cytomine with HTTP request during functional test
 */
class TermAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term.json"
        return doGET(URL, username, password)
    }

    static def listByOntology(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/$id/term.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/term.json"
        return doGET(URL, username, password)
    }

    static def statsTerm(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$id/project/stat.json"
        return doGET(URL, username, password)
    }

    static def create(def jsonTerm, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term.json"
        def result = doPOST(URL,jsonTerm,username,password)
        def json = JSON.parse(result.data)
        if(JSON.parse(jsonTerm) instanceof JSONArray) return [code: result.code]
        Long idTerm = json?.term?.id
        return [data: Term.get(idTerm), code: result.code]
    }

    static def update(def id, def jsonTerm, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        return doPUT(URL,jsonTerm,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        return doDELETE(URL,username,password)
    }

}
