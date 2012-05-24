package be.cytomine
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import be.cytomine.security.User
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.test.http.UserAPI
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class UserTests extends functionaltestplugin.FunctionalTestCase {

    void testListUserWithCredential() {
        def result = UserAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }
  
    void testListUserWithoutCredential() {
        def result = UserAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assertEquals(401, result.code)
    }
  
    void testShowUserWithCredential() {
        def result = UserAPI.show(BasicInstance.createOrGetBasicUser().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }
  
    void testAddUserCorrect() {
        def userToAdd = BasicInstance.getBasicUserNotExist()
        def result = UserAPI.create(userToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idUser = result.data.id
  
        result = UserAPI.show(idUser, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }
  
    void testAddUserAlreadyExist() {
        def userToAdd = BasicInstance.createOrGetBasicUser()
        def result = UserAPI.create(userToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(409, result.code)
    }

    void testAddUserNameAlreadyExist() {
        def user = BasicInstance.createOrGetBasicUser()
        def userToAdd = BasicInstance.getBasicUserNotExist()
        userToAdd.username = user.username
        def result = UserAPI.create(userToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(409, result.code)
    }


    void testAddUserInvalidEmail() {
        def user = BasicInstance.createOrGetBasicUser()
        def userToAdd = BasicInstance.getBasicUserNotExist()
        userToAdd.email = "invalid@email"
        def result = UserAPI.create(userToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testUpdateUserCorrect() {
        User userToAdd = BasicInstance.createOrGetBasicUser()
        def result = UserAPI.update(userToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idUser = json.user.id
  
        def showResult = UserAPI.show(idUser, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareUser(result.mapNew, json)
    }
  
    void testUpdateUserNotExist() {
        User userWithOldName = BasicInstance.createOrGetBasicUser()
        User userWithNewName = BasicInstance.getBasicUserNotExist()
        userWithNewName.save(flush: true)
        User userToEdit = User.get(userWithNewName.id)
        def jsonUser = userToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonUser)
        jsonUpdate.username = "-99"
        jsonUpdate.id = -99
        jsonUser = jsonUpdate.encodeAsJSON()
        def result = UserAPI.update(-99, jsonUser, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }
  
    void testUpdateUserWithNameAlreadyExist() {
        User userWithOldName = BasicInstance.createOrGetBasicUser()
        User userWithNewName = BasicInstance.getBasicUserNotExist()
        userWithNewName.save(flush: true)
        User userToEdit = User.get(userWithNewName.id)
        def jsonUser = userToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonUser)
        jsonUpdate.username = userWithOldName.username
        jsonUser = jsonUpdate.encodeAsJSON()
        def result = UserAPI.update(userToEdit.id, jsonUser, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(409, result.code)
    }

    void testDeleteUser() {
        def userToDelete = BasicInstance.getBasicUserNotExist()
        assert userToDelete.save(flush: true)!= null
        def id = userToDelete.id
        def result = UserAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
  
        def showResult = UserAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)
    }
  
    void testDeleteUserNotExist() {
        def result = UserAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }
    
    void testDeleteMe() {
        def result = UserAPI.delete(User.findByUsername(Infos.GOODLOGIN).id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(403, result.code)
    }    
  
        void testDeleteUserWithDate() {
        def userToDelete = BasicInstance.createOrGetBasicUser()
        def image =  BasicInstance.createOrGetBasicImageInstance()
        image.user = userToDelete
        assert image.save(flush:true)!=null
        def result = UserAPI.delete(userToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    /*void testAddUserChildCorrect() {
       log.info("create user")
       def parent = User.findByUsername(Infos.GOODLOGIN);
       def json = "{parent:"+ parent.id +", username:"+ Math.random()+"}";

       log.info("post user child")
       String URL = Infos.CYTOMINEURL+"api/userJob.json"
       HttpClient client = new HttpClient()
       client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
       client.post(json.toString())
       int code  = client.getResponseCode()
       String response = client.getResponseData()
       println response
       client.disconnect();

       log.info("check response")
       assertEquals(200,code)
       json = JSON.parse(response)
       assert json instanceof JSONObject
       int idUser = json.userJob.id

     }*/
}
