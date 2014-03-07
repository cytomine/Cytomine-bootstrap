package be.cytomine.test.http

import be.cytomine.processing.ImageFilterProject
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageFilterProject to Cytomine with HTTP request during functional test
 */
class ImageFilterProjectAPI extends DomainAPI {


    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagefilterproject.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/imagefilterproject.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagefilterproject.json"
        def result = doPOST(URL,json,username,password)
        def jsonResponse = JSON.parse(result.data)
        def id = jsonResponse.imagefilterproject.id
        return [data: ImageFilterProject.get(id), code: result.code]
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagefilterproject/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
