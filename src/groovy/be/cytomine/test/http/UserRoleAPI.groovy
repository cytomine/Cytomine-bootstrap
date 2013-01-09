package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.security.User
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.security.SecUserSecRole

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage User role to Cytomine with HTTP request during functional test
 */
class UserRoleAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long idUser, Long idRole, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/${idUser}/role/${idRole}.json"
        return doGET(URL, username, password)
    }

    static def listRole(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/role.json"
        return doGET(URL, username, password)
    }

    static def listByUser(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/role.json"
        return doGET(URL, username, password)
    }

    static def create(Long idUser, Long idRole, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/${idUser}/role.json"
        def result = doPOST(URL,json,username,password)
        return result
    }

    static def delete(Long idUser, Long idRole, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/${idUser}/role/${idRole}.json"
        return doDELETE(URL,username,password)
    }
}
