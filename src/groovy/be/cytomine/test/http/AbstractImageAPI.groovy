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

import be.cytomine.image.AbstractImage
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AbstractImage to Cytomine with HTTP request during functional test
 */
class AbstractImageAPI extends DomainAPI {

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage.json"
        return doGET(URL, username, password)
    }

    static def list(boolean datatable,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage.json?datatable=${datatable}&_search=false&nd=1358249507672&rows=10&page=1&sidx=id&sord=asc"
        return doGET(URL, username, password)
    }

    static def list(boolean datatable,Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage.json?project=$idProject&datatables=${datatable}&_search=false&nd=1358249507672&rows=10&page=1&sidx=id&sord=asc"
        return doGET(URL, username, password)
    }

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/image.json"
        return doGET(URL, username, password)
    }

    static def getInfo(Long id, String type,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage/" + id + "/${type}.json"
        return doGET(URL, username, password)
    }

    static def getProperty(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/be.cytomine.image.AbstractImage/" + id + "/property.json"
        return doGET(URL, username, password)
    }

    static def getCrop(Long id, String type,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/$type/$id/crop.json"
        return doGET(URL, username, password)
    }

    static def create(String jsonAbstractImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage.json"
        def result = doPOST(URL, jsonAbstractImage,username, password)
        if(JSON.parse(jsonAbstractImage) instanceof JSONArray) return result
        result.data = AbstractImage.read(JSON.parse(result.data)?.abstractimage?.id)
        return result
    }

    static def update(def id, def jsonAbstractImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage/" + id + ".json"
        return doPUT(URL,jsonAbstractImage,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage/" + id + ".json"
        return doDELETE(URL,username,password)
    }

}
