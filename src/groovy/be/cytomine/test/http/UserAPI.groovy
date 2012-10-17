package be.cytomine.test.http

import be.cytomine.security.User
import be.cytomine.test.BasicInstance
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
class UserAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show user $id"
        String URL = Infos.CYTOMINEURL + "api/user/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list user"
        String URL = Infos.CYTOMINEURL + "api/user.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(User userToAdd, User user) {
       create(userToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(User userToAdd, String username, String password) {
        return create(userToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create user"
        String URL = Infos.CYTOMINEURL + "api/user.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idUser = jsonResponse?.user?.id
        return [data: User.get(idUser), code: code]
    }

    static def update(User user, String username, String password) {
        log.info "update user"
        String oldFirstname = "Firstname1"
        String newFirstname = "Firstname2"

        String oldLastname = "Lastname1"
        String newLastname = "Lastname2"

        String oldEmail = "old@email.com"
        String newEmail = "new@email.com"

        String oldUsername = "Username1"
        String newUsername = "Username2"


        def mapOld = ["firstname":oldFirstname,"lastname":oldLastname,"email":oldEmail,"username":oldUsername]
        def mapNew = ["firstname":newFirstname,"lastname":newLastname,"email":newEmail,"username":newUsername]


        /* Create a Name1 user */
        log.info("create user")
        User userToAdd = BasicInstance.createOrGetBasicUser()
        userToAdd.firstname = oldFirstname
        userToAdd.lastname = oldLastname
        userToAdd.email = oldEmail
        userToAdd.username = oldUsername
        assert (userToAdd.save(flush:true) != null)

        /* Encode a niew user Name2*/
        User userToEdit = User.get(userToAdd.id)
        def jsonUser = userToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonUser)
        jsonUpdate.firstname = newFirstname
        jsonUpdate.lastname = newLastname
        jsonUpdate.email = newEmail
        jsonUpdate.username = newUsername
        jsonUser = jsonUpdate.encodeAsJSON()
        def data = update(user.id, jsonUser, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonUser, String username, String password) {
        log.info "delete user"
        String URL = Infos.CYTOMINEURL + "api/user/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonUser)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete user"
        String URL = Infos.CYTOMINEURL + "api/user/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
