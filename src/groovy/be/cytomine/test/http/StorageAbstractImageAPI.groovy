package be.cytomine.test.http

import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 20/02/13
 * Time: 11:03
 */
class StorageAbstractImageAPI extends DomainAPI {

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage_abstract_image.json"
        def result =  doPOST(URL,json,username,password)
        result.data = StorageAbstractImage.get(JSON.parse(result.data)?.storageabstractimage.id)
        return result
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/storage_abstract_image/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
