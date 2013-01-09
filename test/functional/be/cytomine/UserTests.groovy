package be.cytomine

import be.cytomine.security.User
import be.cytomine.utils.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import be.cytomine.test.http.ProjectAPI
import be.cytomine.utils.UpdateData

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

    void testListUserGrid() {
        def result = UserAPI.grid(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testListUserWithKey() {
        def result = UserAPI.list(BasicInstance.newUser.publicKey,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        println json
        assert json.id==BasicInstance.newUser.id
    }
  
    void testListUserWithoutCredential() {
        def result = UserAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assertEquals(401, result.code)
    }


    void testListFriends() {
        def user = BasicInstance.newUser
        def project = BasicInstance.createOrGetBasicProject()
        def result = UserAPI.listFriends(user.id,false,project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = UserAPI.listFriends(user.id,true,project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = UserAPI.listFriends(user.id,false,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = UserAPI.listFriends(user.id,true,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testListOnlineFriendsWithOpenedImages() {
        def project = BasicInstance.createOrGetBasicProject()
        def result = UserAPI.listOnline(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testShowUserWithCredential() {
        def result = UserAPI.show(BasicInstance.createOrGetBasicUser().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testShowCurrentUser() {
        def result = UserAPI.showCurrent(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }



    void testListProjectUser() {
        def project = BasicInstance.createOrGetBasicProject()
        def result = UserAPI.list(project.id,"project","user",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAPI.list(project.id,"project","user",true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAPI.list(-99,"project","user",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListProjectAdmin() {
        def project = BasicInstance.createOrGetBasicProject()
        def result = UserAPI.list(project.id,"project","admin",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAPI.list(-99,"project","admin",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListProjectCreator() {
        def project = BasicInstance.createOrGetBasicProject()
        def result = UserAPI.list(project.id,"project","creator",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAPI.list(-99,"project","creator",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListOntologyUser() {
        def project = BasicInstance.createOrGetBasicProject()
        def result = UserAPI.list(project.ontology.id,"ontology","user",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAPI.list(-99,"ontology","user",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListOntologyCreator() {
        def project = BasicInstance.createOrGetBasicProject()
        def result = UserAPI.list(project.ontology.id,"ontology","creator",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAPI.list(-99,"ontology","creator",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListProjectLayer() {
        def project = BasicInstance.createOrGetBasicProject()
        def result = UserAPI.list(project.id,"project","userlayer",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAPI.list(-99,"project","userlayer",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testAddUserCorrect() {
        User userToAdd = BasicInstance.getBasicUserNotExist()
        def jsonUser = new JSONObject(userToAdd.encodeAsJSON()).put("password", "password").toString()
        println "jsonUser =" + jsonUser
        def result = UserAPI.create(jsonUser.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
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
        def data = UpdateData.createUpdateSet(userToAdd)
        def result = UserAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idUser = json.user.id
  
        def showResult = UserAPI.show(idUser, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareUser(data.mapNew, json)
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

    void testAddDeleteUserToProject() {
        def project = BasicInstance.getBasicProjectNotExist()
        BasicInstance.saveDomain(project)

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(project.id, BasicInstance.newUser.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, resAddUser.code)

        resAddUser = ProjectAPI.deleteUserProject(project.id, BasicInstance.newUser.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, resAddUser.code)
    }

    void testAddDeleteAdminToProject() {
        def project = BasicInstance.getBasicProjectNotExist()
        BasicInstance.saveDomain(project)

        //Add project right for user 2
        def resAddUser = ProjectAPI.addAdminProject(project.id, BasicInstance.newUser.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, resAddUser.code)

        resAddUser = ProjectAPI.deleteAdminProject(project.id, BasicInstance.newUser.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, resAddUser.code)
    }

    /*
          SHOW USER JOB
     */

    void testShowUserJob() {
        def userJob = BasicInstance.createOrGetBasicUserJob()
        def result = UserAPI.showUserJob(userJob.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        result = UserAPI.showUserJob(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListUserJob() {
        def userJob = BasicInstance.createOrGetBasicUserJob()
        def result = UserAPI.listUserJob(userJob.job.project.id,false,null, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }
    void testListUserJobTree() {
        def userJob = BasicInstance.createOrGetBasicUserJob()
        def result = UserAPI.listUserJob(userJob.job.project.id,true,null, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }
    void testListUserJobByImages() {
        def userJob = BasicInstance.createOrGetBasicUserJob()
        def result = UserAPI.listUserJob(userJob.job.project.id,false,BasicInstance.createOrGetBasicImageInstance().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }



    void testAddUserChildCorrect() {
       log.info("create user")
       def parent = User.findByUsername(Infos.GOODLOGIN);
       def json = "{parent:"+ parent.id +", username:"+ Math.random()+", software: ${BasicInstance.createOrGetBasicSoftware().id}}";

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
     }




}
