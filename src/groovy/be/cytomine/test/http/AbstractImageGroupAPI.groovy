package be.cytomine.test.http

import be.cytomine.image.AbstractImageGroup
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AbstractImageGroup to Cytomine with HTTP request during functional test
 */
class AbstractImageGroupAPI extends DomainAPI {

    static def listByImage(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/$id/group.json"
        return doGET(URL, username, password)
    }

    static def listByGroup(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/group/$id/image.json"
        return doGET(URL, username, password)
    }

    static def listByUser(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/image.json"
        return doGET(URL, username, password)
    }

    static def show(Long idImage, Long idGroup, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/" + idImage + "/group/" + idGroup + ".json"
        return doGET(URL, username, password)
    }

    static def create(Long idImage, Long idGroup,String jsonAbstractImageGroup, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/$idImage/group/$idGroup" + ".json"
        def result = doPOST(URL, jsonAbstractImageGroup,username, password)
        result.data = AbstractImageGroup.read(JSON.parse(result.data)?.abstractimagegroup?.id)
        return result
    }

    static def delete(Long idImage, Long idGroup,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/$idImage/group/$idGroup" + ".json"
        return doDELETE(URL,username,password)
    }
}
