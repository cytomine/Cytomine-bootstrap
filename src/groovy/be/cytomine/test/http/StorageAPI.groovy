package be.cytomine.test.http

import be.cytomine.image.server.Storage
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 7/02/13
 * Time: 15:06
 */
class StorageAPI  extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage.json"
        def result = doPOST(URL,json,username,password)
        result.data = Storage.get(JSON.parse(result.data)?.storage.id)
        return result
    }

    static def update(def id, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage/" + id + ".json"
        return doPUT(URL,json,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}