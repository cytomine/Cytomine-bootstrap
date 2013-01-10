package be.cytomine.test.http

import be.cytomine.test.Infos
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class UserPositionAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def listLastByUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/imageinstance/$idImage/position/${idUser}.json"
        return doGET(URL, username, password)
    }

    static def listLastByProject(Long idProject,String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/project/$idProject/online.json"
        return doGET(URL, username, password)
    }

    static def listLastByImage(Long idImage,String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/imageinstance/$idImage/online.json"
        return doGET(URL, username, password)
    }

    static def create(Long idImage, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/imageinstance/$idImage/position.json"
        def result = doPOST(URL,json,username,password)
        return result
    }
}
