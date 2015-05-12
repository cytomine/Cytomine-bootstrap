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

import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage RelationTerm to Cytomine with HTTP request during functional test
 */
class RelationTermAPI extends DomainAPI {

    static def show(Long idRelation, Long idTerm1, Long idTerm2,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/relation/" + idRelation + "/term1/"+ idTerm1 +"/term2/"+idTerm2+".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/relation.json"
        return doGET(URL, username, password)
    }

    static def listByTermAll(Long idTerm,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/relation/term/"+idTerm+".json"
        return doGET(URL, username, password)
    }

    static def listByTerm(Long idRelation,Long indexTerm,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/relation/term/"+indexTerm+"/" + idRelation +  ".json"
        return doGET(URL, username, password)
    }

    static def create(String jsonRelationTerm, String username, String password) {
        create(jsonRelationTerm,username,password,false)
    }
    
    static def create(String jsonRelationTerm, String username, String password, boolean deleteOldTerm) {
        def json = JSON.parse(jsonRelationTerm);
        String URL = Infos.CYTOMINEURL+"api/relation/"+ json.relation +"/term.json"
        def result = doPOST(URL,jsonRelationTerm,username,password)
        return result
    }    

    static def delete(def idRelation, def idTerm1, def idTerm2, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/relation/"+idRelation + "/term1/"+idTerm1+"/term2/"+idTerm2+".json"
        return doDELETE(URL,username,password)
    }
}
