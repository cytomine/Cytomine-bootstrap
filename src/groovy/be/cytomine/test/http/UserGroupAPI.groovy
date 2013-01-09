package be.cytomine.test.http

import be.cytomine.security.User
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage User to Cytomine with HTTP request during functional test
 */
class UserGroupAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def showUserGroupCurrent(Long idUser, Long idGroup,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$idUser/group/${idGroup}.json"
        return doGET(URL, username, password)
    }

    static def list(Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$idUser/group.json"
        return doGET(URL, username, password)
    }

    static def create(Long idUser,String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$idUser/group.json"
        def result = doPOST(URL,json,username,password)
        return [data: null, code: result.code]
    }

    static def delete(Long idUser, Long idGroup, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$idUser/group/${idGroup}.json"
        return doDELETE(URL,username,password)
    }
}
