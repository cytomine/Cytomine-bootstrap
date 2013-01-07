package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
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
        log.info "show role $idUser/$idRole"
        String URL = Infos.CYTOMINEURL + "api/user/${idUser}/role/${idRole}.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listRole(String username, String password) {
        log.info "list role"
        String URL = Infos.CYTOMINEURL + "api/role.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByUser(Long id,String username, String password) {
        log.info "list role by user "
        String URL = Infos.CYTOMINEURL + "api/user/$id/role.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(SecUserSecRole secUserSecRoleToAdd, User user) {
       create(secUserSecRoleToAdd.encodeAsJSON(),user.username,user.password)
    }

    static def create(Long idUser, Long idRole, def json, String username, String password) {
        log.info "create secUserSecRoleToAdd"
        String URL = Infos.CYTOMINEURL + "api/user/${idUser}/role.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: null, code: code]
    }

    static def delete(Long idUser, Long idRole, String username, String password) {
        log.info "delete secUserSecRoleToAdd"
        String URL = Infos.CYTOMINEURL + "api/user/${idUser}/role/${idRole}.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
