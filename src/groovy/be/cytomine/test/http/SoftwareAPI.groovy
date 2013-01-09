package be.cytomine.test.http

import be.cytomine.processing.Software
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Software to Cytomine with HTTP request during functional test
 */
class SoftwareAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software.json"
        def result = doPOST(URL,json,username,password)
        result.data = Software.get(JSON.parse(result.data)?.software?.id)
        return result
    }

    static def update(def id, def jsonSoftware, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software/" + id + ".json"
        return doPUT(URL,jsonSoftware,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
