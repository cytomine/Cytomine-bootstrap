package be.cytomine.test.http

import be.cytomine.image.multidim.ImageGroup
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageFilter to Cytomine with HTTP request during functional test
 */
class ImageGroupAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagegroup/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$idProject/imagegroup.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagegroup.json"
        def result = doPOST(URL, json,username, password)
        Long idDiscipline = JSON.parse(result.data)?.imagegroup?.id
        return [data: ImageGroup.get(idDiscipline), code: result.code]
    }

    static def update(def id, def jsonImageGroup, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagegroup/" + id + ".json"
        return doPUT(URL,jsonImageGroup,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagegroup/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
