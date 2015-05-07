package be.cytomine

import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.UserAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class UserTests  {

    void testListUserWithCredential() {
        def result = UserAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListUserWithKey() {
        def result = UserAPI.list(BasicInstanceBuilder.user1.publicKey,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        println json
        assert json.id==BasicInstanceBuilder.user1.id
    }
  
    void testListUserWithoutCredential() {
        def result = UserAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }


    void testListFriends() {
        def user = BasicInstanceBuilder.user1
        def project = BasicInstanceBuilder.getProject()
        Infos.addUserRight(user.username,project)
        def result = UserAPI.listFriends(user.id,false,project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = UserAPI.listFriends(user.id,true,project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = UserAPI.listFriends(user.id,false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = UserAPI.listFriends(user.id,true,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testListOnlineFriendsWithOpenedImages() {
        def project = BasicInstanceBuilder.getProject()
        def result = UserAPI.listOnline(project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testShowUserWithId() {
        def result = UserAPI.show(BasicInstanceBuilder.getUser().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testShowUserWithUsername() {
        def result = UserAPI.show(BasicInstanceBuilder.getUser().username, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testShowKeysWithUsername() {
        def user = BasicInstanceBuilder.getUser()
        def result = UserAPI.keys(user.username, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.publicKey.equals(user.publicKey)
        assert json.privateKey.equals(user.privateKey)

        result = UserAPI.keys(user.username, user.username, "password")
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.publicKey.equals(user.publicKey)
        assert json.privateKey.equals(user.privateKey)
    }

    void testShowKeysWithId() {
        def user = BasicInstanceBuilder.getUser()
        def result = UserAPI.keys(user.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.publicKey.equals(user.publicKey)
        assert json.privateKey.equals(user.privateKey)

        result = UserAPI.keys(user.id, user.username, "password")
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.publicKey.equals(user.publicKey)
        assert json.privateKey.equals(user.privateKey)
    }


    void testShowCurrentUser() {
        def result = UserAPI.showCurrent(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }



    void testListProjectUser() {
        def project = BasicInstanceBuilder.getProject()
        def result = UserAPI.list(project.id,"project","user",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAPI.list(project.id,"project","user",true,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAPI.list(-99,"project","user",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListProjectAdmin() {
        def project = BasicInstanceBuilder.getProject()
        def result = UserAPI.list(project.id,"project","admin",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAPI.list(-99,"project","admin",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListProjectCreator() {
        def project = BasicInstanceBuilder.getProject()
        def result = UserAPI.list(project.id,"project","creator",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAPI.list(-99,"project","creator",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListOntologyUser() {
        def project = BasicInstanceBuilder.getProject()
        def result = UserAPI.list(project.ontology.id,"ontology","user",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAPI.list(-99,"ontology","user",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListOntologyCreator() {
        def project = BasicInstanceBuilder.getProject()
        def result = UserAPI.list(project.ontology.id,"ontology","creator",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAPI.list(-99,"ontology","creator",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListProjectLayer() {
        def project = BasicInstanceBuilder.getProject()
        def result = UserAPI.list(project.id,"project","userlayer",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAPI.list(-99,"project","userlayer",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testAddUserCorrect() {
        User userToAdd = BasicInstanceBuilder.getUserNotExist()
        def jsonUser = new JSONObject(userToAdd.encodeAsJSON()).put("password", "password").toString()
        println "jsonUser =" + jsonUser
        def result = UserAPI.create(jsonUser.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idUser = result.data.id
  
        result = UserAPI.show(idUser, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }
  
    void testAddUserAlreadyExist() {
        def userToAdd = BasicInstanceBuilder.getUser()
        def result = UserAPI.create(userToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testAddUserNameAlreadyExist() {
        def user = BasicInstanceBuilder.getUser()
        def userToAdd = BasicInstanceBuilder.getUserNotExist()
        userToAdd.username = user.username
        def result = UserAPI.create(userToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }


    void testAddUserInvalidEmail() {
        def user = BasicInstanceBuilder.getUser()
        def userToAdd = BasicInstanceBuilder.getUserNotExist()
        userToAdd.email = "invalid@email"
        def result = UserAPI.create(userToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

    void testUpdateUserCorrect() {
        def user = BasicInstanceBuilder.getUserNotExist(true)
        def data = UpdateData.createUpdateSet(user,[firstname: ["OLDNAME","NEWNAME"], email:["old@email.com","new@email.com"]])

        def result = UserAPI.update(user.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idUser = json.user.id
  
        def showResult = UserAPI.show(idUser, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
    }
  
    void testUpdateUserNotExist() {
        User userWithOldName = BasicInstanceBuilder.getUser()
        User userWithNewName = BasicInstanceBuilder.getUserNotExist()
        userWithNewName.save(flush: true)
        User userToEdit = User.get(userWithNewName.id)
        def jsonUser = userToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonUser)
        jsonUpdate.username = "-99"
        jsonUpdate.id = -99
        jsonUser = jsonUpdate.toString()
        def result = UserAPI.update(-99, jsonUser, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
  
    void testUpdateUserWithNameAlreadyExist() {
        User userWithOldName = BasicInstanceBuilder.getUser()
        User userWithNewName = BasicInstanceBuilder.getUserNotExist()
        userWithNewName.save(flush: true)
        User userToEdit = User.get(userWithNewName.id)
        def jsonUser = userToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonUser)
        jsonUpdate.username = userWithOldName.username
        jsonUser = jsonUpdate.toString()
        def result = UserAPI.update(userToEdit.id, jsonUser, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testDeleteUser() {
        def userToDelete = BasicInstanceBuilder.getUserNotExist()
        assert userToDelete.save(flush: true)!= null
        def id = userToDelete.id
        def result = UserAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
  
        def showResult = UserAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code
    }
  
    void testDeleteUserNotExist() {
        def result = UserAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
    
    void testDeleteMe() {
        def result = UserAPI.delete(User.findByUsername(Infos.SUPERADMINLOGIN).id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 403==result.code
    }

    void testAddDeleteUserToProject() {
        def project = BasicInstanceBuilder.getProjectNotExist()
        BasicInstanceBuilder.saveDomain(project)

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(project.id, BasicInstanceBuilder.user1.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == resAddUser.code

        resAddUser = ProjectAPI.deleteUserProject(project.id, BasicInstanceBuilder.user1.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == resAddUser.code
    }

    void testAddDeleteAdminToProject() {
        def project = BasicInstanceBuilder.getProjectNotExist()
        BasicInstanceBuilder.saveDomain(project)

        //Add project right for user 2
        def resAddUser = ProjectAPI.addAdminProject(project.id, BasicInstanceBuilder.user1.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == resAddUser.code

        resAddUser = ProjectAPI.deleteAdminProject(project.id, BasicInstanceBuilder.user1.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == resAddUser.code
    }

    /*
          SHOW USER JOB
     */

    void testShowUserJob() {
        def userJob = BasicInstanceBuilder.getUserJob()
        def result = UserAPI.showUserJob(userJob.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        result = UserAPI.showUserJob(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListUserJob() {
        def userJob = BasicInstanceBuilder.getUserJob()
        def result = UserAPI.listUserJob(userJob.job.project.id,false,null, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }
    void testListUserJobTree() {
        def userJob = BasicInstanceBuilder.getUserJob()
        def result = UserAPI.listUserJob(userJob.job.project.id,true,null, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }
    void testListUserJobByImages() {
        def userJob = BasicInstanceBuilder.getUserJob()
        def result = UserAPI.listUserJob(userJob.job.project.id,false,BasicInstanceBuilder.getImageInstance().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }



    void testAddUserChildCorrect() {
       log.info("create user")
       def parent = User.findByUsername(Infos.SUPERADMINLOGIN);
       def json = "{parent:"+ parent.id +", username:"+ Math.random()+", software: ${BasicInstanceBuilder.getSoftware().id}}";

       log.info("post user child")
       String URL = Infos.CYTOMINEURL+"api/userJob.json"
       HttpClient client = new HttpClient()
       client.connect(URL,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
       client.post(json.toString())
       int code  = client.getResponseCode()
       String response = client.getResponseData()
       println response
       client.disconnect();

       log.info("check response")
       assert 200==code
       json = JSON.parse(response)
       assert json instanceof JSONObject
     }

            /**
         if(project.checkPermission(ADMINISTRATION)) {
             return users
         } else if(project.hideAdminsLayers && project.hideUsersLayers && users.contains(currentUser)) {
             return [currentUser]
         } else if(project.hideAdminsLayers && !project.hideUsersLayers && users.contains(currentUser)) {
             users.removeAll(admins)
             return users
         } else if(!project.hideAdminsLayers && project.hideUsersLayers && users.contains(currentUser)) {
             admins.add(currentUser)
             return admins
          }else if(!project.hideAdminsLayers && !project.hideUsersLayers && users.contains(currentUser)) {
             return users
          }else { //should no arrive but possible if user is admin and not in project
              []
          }**/



    void testListLayerAllLayers() {
        def simpleUsername1 = "simpleUserListLayer1"
        def simpleUsername2 = "simpleUserListLayer2"
        def adminUsername = "adminRO"
        def password = "password"
        //create project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        //by default all is visible!
        assert !project.hideUsersLayers
        assert !project.hideAdminsLayers

        //Add a simple project user
        User simpleUser1 = BasicInstanceBuilder.getUser(simpleUsername1,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser1.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //Add a simple project user
        User simpleUser2 = BasicInstanceBuilder.getUser(simpleUsername2,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser2.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //Add a project admin
        User admin = BasicInstanceBuilder.getUser(adminUsername,password)
        assert 200 == ProjectAPI.addAdminProject(project.id,admin.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //a admin must see all layers
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),simpleUser1.id)
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),simpleUser2.id)
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),admin.id)
        //a simple user must see all layers too
        assert checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),simpleUser1.id)
        assert checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),simpleUser2.id)
        assert checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),admin.id)
    }

    void testListLayerAllLayersHideAdminLayers() {
        def simpleUsername1 = "simpleUserListLayer1"
        def simpleUsername2 = "simpleUserListLayer2"
        def adminUsername = "adminRO"
        def password = "password"
        //create project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        project.hideAdminsLayers = true
        BasicInstanceBuilder.saveDomain(project)

        //Add a simple project user
        User simpleUser1 = BasicInstanceBuilder.getUser(simpleUsername1,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser1.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //Add a simple project user
        User simpleUser2 = BasicInstanceBuilder.getUser(simpleUsername2,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser2.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //Add a project admin
        User admin = BasicInstanceBuilder.getUser(adminUsername,password)
        assert 200 == ProjectAPI.addAdminProject(project.id,admin.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //a admin must see all layers
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),simpleUser1.id)
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),simpleUser2.id)
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),admin.id)
        //a simple user must see all layers exept admins layer
        assert checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),simpleUser1.id)
        assert checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),simpleUser2.id)
        assert !checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),admin.id)
    }

    void testListLayerAllLayersHideUserLayers() {
        def simpleUsername1 = "simpleUserListLayer1"
        def simpleUsername2 = "simpleUserListLayer2"
        def adminUsername = "adminRO"
        def password = "password"
        //create project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        project.hideUsersLayers = true
        BasicInstanceBuilder.saveDomain(project)

        //Add a simple project user
        User simpleUser1 = BasicInstanceBuilder.getUser(simpleUsername1,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser1.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //Add a simple project user
        User simpleUser2 = BasicInstanceBuilder.getUser(simpleUsername2,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser2.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //Add a project admin
        User admin = BasicInstanceBuilder.getUser(adminUsername,password)
        assert 200 == ProjectAPI.addAdminProject(project.id,admin.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //a admin must see all layers
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),simpleUser1.id)
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),simpleUser2.id)
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),admin.id)
        //a simple user must see all admisn layer and his layer
        assert checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),simpleUser1.id)
        assert !checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),simpleUser2.id)
        assert checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),admin.id)
    }

    void testListLayerAllLayersHideUserLayersAndHideAdminLayers() {
        def simpleUsername1 = "simpleUserListLayer1"
        def simpleUsername2 = "simpleUserListLayer2"
        def adminUsername = "adminRO"
        def password = "password"
        //create project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        project.hideUsersLayers = true
        project.hideAdminsLayers = true
        BasicInstanceBuilder.saveDomain(project)

        //Add a simple project user
        User simpleUser1 = BasicInstanceBuilder.getUser(simpleUsername1,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser1.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //Add a simple project user
        User simpleUser2 = BasicInstanceBuilder.getUser(simpleUsername2,password)
        assert 200 == ProjectAPI.addUserProject(project.id,simpleUser2.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //Add a project admin
        User admin = BasicInstanceBuilder.getUser(adminUsername,password)
        assert 200 == ProjectAPI.addAdminProject(project.id,admin.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code

        //a admin must see all layers
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),simpleUser1.id)
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),simpleUser2.id)
        assert checkIfContains(UserAPI.listLayers(project.id,adminUsername,password),admin.id)
        //a simple user must see only its own layer
        assert checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),simpleUser1.id)
        assert !checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),simpleUser2.id)
        assert !checkIfContains(UserAPI.listLayers(project.id,simpleUsername1,password),admin.id)
    }

    static boolean checkIfContains(def result, def id) {
        assert 200 == result.code
        def json = JSON.parse(result.data)
        return UserAPI.containsInJSONList(id,json)
    }


    void testAPIGetSignature() {
         ///api/signature
         assert 200 == UserAPI.signature(Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code
     }


    void testResetPassword() {
        User user = BasicInstanceBuilder.getUserNotExist(true)

        //just call a simple service to check
        assert 200 == UserAPI.signature(user.username,"password").code

        //change password
        def response = UserAPI.resetPassword(user.id,"newpassword",Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == response.code
        println response
        //just call a simple service to check if password  has changed
        assert 200 == UserAPI.signature(user.username,"newpassword").code
        assert 401 == UserAPI.signature(user.username,"password").code

    }

    void testResetPasswordWithBadUser() {
        //change password
        assert 404 == UserAPI.resetPassword(-99,"newpassword",Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code
    }

    void testResetPasswordWithBadPassword() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        assert 404 == UserAPI.resetPassword(user.id,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code
        assert 400 == UserAPI.resetPassword(user.id,"bad",Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code
    }


    void testResetPasswordWithUnauthUser() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        User anotherUser = BasicInstanceBuilder.getUserNotExist(true)

        //just call a simple service to check
        assert 200 == UserAPI.signature(user.username,"password").code

        //change password
        assert 200 == UserAPI.resetPassword(user.id,"newpassword",user.username,"password").code

        //just call a simple service to check if password  has changed
        assert 200 == UserAPI.signature(user.username,"newpassword").code
        assert 401 == UserAPI.signature(user.username,"password").code

        //change password with another user credential
        assert 403 == UserAPI.resetPassword(user.id,"unauthpassword",anotherUser.username,"password").code
        //just call a simple service to check if password  has changed
        assert 401 == UserAPI.signature(user.username,"unauthpassword").code
        assert 200 == UserAPI.signature(user.username,"newpassword").code
    }

//    void testLDAP() {
//        def result = UserAPI.isInLDAP("u212435",Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
//        assert 200 == result.code
//        assert true == JSON.parse(result.data).result
//    }

}
