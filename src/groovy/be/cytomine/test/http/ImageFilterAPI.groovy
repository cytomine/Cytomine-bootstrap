package be.cytomine.test.http

import be.cytomine.test.Infos

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageFilter to Cytomine with HTTP request during functional test
 */
class ImageFilterAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagefilter/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagefilter.json"
        return doGET(URL, username, password)
    }
}
