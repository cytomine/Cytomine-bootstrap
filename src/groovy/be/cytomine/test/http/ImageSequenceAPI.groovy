package be.cytomine.test.http

import be.cytomine.image.multidim.ImageSequence
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageFilter to Cytomine with HTTP request during functional test
 */
class ImageSequenceAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagesequence/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(Long idImageGroup, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagegroup/$idImageGroup/imagesequence.json"
        return doGET(URL, username, password)
    }

    static def get(Long idImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImageInstance/imagesequence.json"
        return doGET(URL, username, password)
    }

    static def getSequenceInfo(Long idImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImageInstance/imagesequence/possibilities.json"
        return doGET(URL, username, password)
    }

    static def get(Long idImageInstance, Integer channel,Integer zStack, Integer slice, Integer time, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/imagegroup/$idImageInstance/$channel/$zStack/$slice/$time/imagesequence.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagesequence.json"
        def result = doPOST(URL, json,username, password)
        Long idImage = JSON.parse(result.data)?.imagesequence?.id
        return [data: ImageSequence.get(idImage), code: result.code]
    }

    static def update(def id, def jsonImageGroup, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagesequence/" + id + ".json"
        return doPUT(URL,jsonImageGroup,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagesequence/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
