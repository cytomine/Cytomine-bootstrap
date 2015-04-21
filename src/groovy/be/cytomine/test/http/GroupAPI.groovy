package be.cytomine.test.http

import be.cytomine.security.Group
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class GroupAPI extends DomainAPI {

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

    static def isInLDAP(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ldap/$id/group.json"
        return doGET(URL, username, password)
    }

    static def createFromLDAP(String json,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ldap/group.json"
        def result = doPOST(URL, json, username, password)
        Long idGroup = JSON.parse(result.data)?.data?.group?.id
        return [data: Group.get(idGroup).encodeAsJSON(), code: result.code]
    }

    static def resetFromLDAP(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ldap/$id/group.json"
        return doPUT(URL, "", username, password)
    }
}
