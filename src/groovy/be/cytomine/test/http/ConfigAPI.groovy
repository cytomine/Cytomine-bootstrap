package be.cytomine.test.http

import be.cytomine.test.Infos
import be.cytomine.utils.Config
import grails.converters.JSON

/**
 * Created by hoyoux on 06.05.15.
 */
class ConfigAPI extends DomainAPI {

    //SHOW
    static def show(String key, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/config/key/${key}.json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/config.json"
        return doGET(URL, username, password)
    }

    //ADD
    static def create(String json, String username, String password) {

        String URL = Infos.CYTOMINEURL + "api/config.json"
        def result = doPOST(URL,json,username,password)
        result.data = Config.get(JSON.parse(result.data)?.config?.id)
        return result
    }

    //UPDATE
    static def update(String key, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/config/key/${key}.json"
        return doPUT(URL,json,username,password)
    }

    //DELETE
    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/config/${id}.json"
        return doDELETE(URL,username,password)
    }
}
