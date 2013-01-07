package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
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
        log.info "show group $id"
        String URL = Infos.CYTOMINEURL + "api/group/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list group"
        String URL = Infos.CYTOMINEURL + "api/group.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def grid(String username, String password) {
        log.info "list group"
        String URL = Infos.CYTOMINEURL + "api/group/grid.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(Group groupToAdd, User user) {
       create(groupToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(Group groupToAdd, String username, String password) {
        return create(groupToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create group"
        String URL = Infos.CYTOMINEURL + "api/group.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idGroup = jsonResponse?.group?.id
        return [data: Group.get(idGroup), code: code]
    }

    static def update(Group group, String username, String password) {
        log.info "update group"
        String oldName = "NAME1"
         String newName = "NAME2"
         def mapNew = ["name":newName]
         def mapOld = ["name":oldName]
         /* Create a Name1 group */
         Group groupToAdd = BasicInstance.createOrGetBasicGroup()
         groupToAdd.name = oldName
         assert (groupToAdd.save(flush:true) != null)
         /* Encode a niew group Name2*/
         Group groupToEdit = Group.get(groupToAdd.id)
         def jsonGroup = groupToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonGroup)
         jsonUpdate.name = newName
         jsonGroup = jsonUpdate.encodeAsJSON()
        def data = update(group.id, jsonGroup, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonGroup, String username, String password) {
        log.info "delete group"
        String URL = Infos.CYTOMINEURL + "api/group/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonGroup)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete group"
        String URL = Infos.CYTOMINEURL + "api/group/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
