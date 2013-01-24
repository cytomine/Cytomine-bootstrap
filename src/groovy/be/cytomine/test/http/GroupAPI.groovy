package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.security.Group

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class GroupAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/group/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/group.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/group.json"
        def result = doPOST(URL, json,username, password)
        Long idGroup = JSON.parse(result.data)?.group?.id
        return [data: Group.get(idGroup), code: result.code]
    }

    static def update(def id, def jsonGroup, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/group/" + id + ".json"
        return doPUT(URL,jsonGroup,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/group/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
