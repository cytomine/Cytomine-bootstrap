package be.cytomine.test.http

import be.cytomine.processing.SoftwareParameter
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage SoftwareParameterAPI to Cytomine with HTTP request during functional test
 */
class SoftwareParameterAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareparameter/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareparameter.json"
        return doGET(URL, username, password)
    }

    static def listBySoftware(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software/$id/parameter.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareparameter.json"
        def result = doPOST(URL,json,username,password)
        result.data = SoftwareParameter.get(JSON.parse(result.data)?.softwareparameter?.id)
        return result
    }

    static def update(def id, def jsonSoftwareParameter, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareparameter/" + id + ".json"
        return doPUT(URL,jsonSoftwareParameter,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareparameter/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
