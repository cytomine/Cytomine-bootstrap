package be.cytomine.security

import be.cytomine.project.Project

import be.cytomine.test.http.ProjectAPI

import be.cytomine.test.BasicInstance
import grails.converters.JSON
import be.cytomine.test.http.UserAPI
import be.cytomine.image.ImageInstance

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class UserSecurityTests extends SecurityTestsAbstract {


    void testUserSecurityForCytomineAdmin() {
        //Get user 1
        User user1 = BasicInstance.createOrGetBasicUser(USERNAMEWITHOUTDATA,PASSWORDWITHOUTDATA)

        //Get user admin
        User admin = BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)

        //Check if admin can read/add/update/del
        assertEquals(200, UserAPI.create(BasicInstance.getBasicUserNotExist().encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
        assertEquals(200, UserAPI.show(user1.id,USERNAMEADMIN,PASSWORDADMIN).code)
        assertTrue(UserAPI.containsInJSONList(user1.id,JSON.parse(UserAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
        assertEquals(200, UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)

        //check if admin can add/del user from project
        Project project = BasicInstance.createBasicProjectNotExist()
        assertEquals(200, ProjectAPI.addUserProject(project.id,user1.id,USERNAMEADMIN,PASSWORDADMIN).code)
        assertEquals(200, ProjectAPI.deleteUserProject(project.id,user1.id,USERNAMEADMIN,PASSWORDADMIN).code)

        //Check if admin can del
        ImageInstance.list().each{
            println "Image ${it.id} => ${it.user.id}"
        }

        assertEquals(200, UserAPI.delete(user1.id,USERNAMEADMIN,PASSWORDADMIN).code)
    }

    void testUserSecurityForHimself() {
        //Get user 1
        User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

        //Check if himself can read/add/update/del
        assertEquals(403, UserAPI.create(BasicInstance.getBasicUserNotExist().encodeAsJSON(),USERNAME1,PASSWORD1).code)
        assertEquals(200, UserAPI.show(user1.id,USERNAME1,PASSWORD1).code)
        assertTrue(UserAPI.containsInJSONList(user1.id,JSON.parse(UserAPI.list(USERNAME1,PASSWORD1).data)))
        assertEquals(200, UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAME1,PASSWORD1).code)

        //check if himself can add/del user from project
        Project project = BasicInstance.createBasicProjectNotExist()
        assertEquals(403, ProjectAPI.addUserProject(project.id,user1.id,USERNAME1,PASSWORD1).code)
        assertEquals(200, ProjectAPI.deleteUserProject(project.id,user1.id,USERNAME1,PASSWORD1).code)

        //Check if himself can del
        assertEquals(403, UserAPI.delete(user1.id,USERNAME1,PASSWORD1).code)
    }

    void testUserSecurityForAnotherUser() {
        //Get user 1
        User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

        //Get user 2
        User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

        //Check if another user can read/add/update/del
        assertEquals(403, UserAPI.create(BasicInstance.getBasicUserNotExist().encodeAsJSON(),USERNAME2,PASSWORD2).code)
        assertEquals(200, UserAPI.show(user1.id,USERNAME2,PASSWORD2).code)
        assertTrue(UserAPI.containsInJSONList(user1.id,JSON.parse(UserAPI.list(USERNAME2,PASSWORD2).data)))
        assertEquals(403, UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAME2,PASSWORD2).code)

        //check if another user can add/del user from project
        Project project = BasicInstance.createBasicProjectNotExist()
        assertEquals(403, ProjectAPI.addUserProject(project.id,user1.id,USERNAME2,PASSWORD2).code)
        assertEquals(403, ProjectAPI.deleteUserProject(project.id,user1.id,USERNAME2,PASSWORD2).code)

        //Check if another user can del
        assertEquals(403, UserAPI.delete(user1.id,USERNAME2,PASSWORD2).code)
    }

    void testUserSecurityForNotConnectedUser() {

        //Check if a non connected user can read/add/update/del
        assertEquals(401, UserAPI.create(BasicInstance.getBasicUserNotExist().encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
        assertEquals(401, UserAPI.show(user1.id,USERNAMEBAD,PASSWORDBAD).code)
        assertEquals(401, UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)

        //check if a non connected user  can add/del user from project
        Project project = BasicInstance.createBasicProjectNotExist()
        assertEquals(401, ProjectAPI.addUserProject(project.id,user1.id,USERNAMEBAD,PASSWORDBAD).code)
        assertEquals(401, ProjectAPI.deleteUserProject(project.id,user1.id,USERNAMEBAD,PASSWORDBAD).code)

        //Check if a non connected user  can del
        assertEquals(401, UserAPI.delete(user1.id,USERNAMEBAD,PASSWORDBAD).code)
    }
}
