package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.image.NestedImageInstance
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageInstance to Cytomine with HTTP request during functional test
 */
class NestedImageInstanceAPI extends DomainAPI {

    static def listByImageInstance(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/nested.json"
        return doGET(URL, username, password)
    }

    static def show(Long id,Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImage/nested/${id}.json"
        return doGET(URL, username, password)
    }


    static def create(Long idImage,String jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImage/nested.json"
        def result = doPOST(URL,jsonImageInstance,username,password)
        result.data = NestedImageInstance.get(JSON.parse(result.data)?.nestedimageinstance?.id)
        return result
    }

    static def update(Long id, Long idImage, def jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImage/nested/${id}.json"
        return doPUT(URL,jsonImageInstance,username,password)
    }

    static def delete(Long id, Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImage/nested/${id}.json"
        return doDELETE(URL,username,password)
    }

}
