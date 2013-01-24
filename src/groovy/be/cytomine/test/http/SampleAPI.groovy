package be.cytomine.test.http

import be.cytomine.laboratory.Sample
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Sample to Cytomine with HTTP request during functional test
 */
class SampleAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/sample/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/sample.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/sample.json"
        def result = doPOST(URL, json,username, password)
        Long idSample = JSON.parse(result.data)?.sample?.id
        return [data: Sample.get(idSample), code: result.code]
    }

    static def update(def id, def jsonSample, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/sample/" + id + ".json"
        return doPUT(URL,jsonSample,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/sample/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
