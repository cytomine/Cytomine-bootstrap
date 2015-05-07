package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.UserAPI
import grails.converters.JSON

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
        User user1 = BasicInstanceBuilder.getUser(USERNAMEWITHOUTDATA,PASSWORDWITHOUTDATA)

        //Get user admin
        User admin = BasicInstanceBuilder.getSuperAdmin(USERNAMEADMIN,PASSWORDADMIN)

        //Check if admin can read/add/update/del
        assert (200 == UserAPI.create(BasicInstanceBuilder.getUserNotExist().encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
        assert (200 == UserAPI.show(user1.id,USERNAMEADMIN,PASSWORDADMIN).code)
        assert (200 == UserAPI.keys(user1.username,USERNAMEADMIN, PASSWORDADMIN).code)
        assert (true ==UserAPI.containsInJSONList(user1.id,JSON.parse(UserAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
        assert (200 == UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)

        //check if admin can add/del user from project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        assert (200 == ProjectAPI.addUserProject(project.id,user1.id,USERNAMEADMIN,PASSWORDADMIN).code)
        assert (200 == ProjectAPI.deleteUserProject(project.id,user1.id,USERNAMEADMIN,PASSWORDADMIN).code)

        assert (200 == UserAPI.delete(user1.id,USERNAMEADMIN,PASSWORDADMIN).code)
    }

    void testUserSecurityForHimself() {
        //Get user 1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
        println SecUserSecRole.findAllBySecUser(user1).collect{it.secRole.authority}
        //Check if himself can read/add/update/del
        assert (200 == UserAPI.create(BasicInstanceBuilder.getUserNotExist().encodeAsJSON(),USERNAME1,PASSWORD1).code)
        assert (200 == UserAPI.show(user1.id,USERNAME1,PASSWORD1).code)
        assert (200 == UserAPI.keys(user1.username,USERNAME1, PASSWORD1).code)
        assert (true ==UserAPI.containsInJSONList(user1.id,JSON.parse(UserAPI.list(USERNAME1,PASSWORD1).data)))
        assert (200 == UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAME1,PASSWORD1).code)

        //check if himself can add/del user from project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        assert (403 == ProjectAPI.addUserProject(project.id,user1.id,USERNAME1,PASSWORD1).code)
        //assert (200 == ProjectAPI.deleteUserProject(project.id,user1.id,USERNAME1,PASSWORD1).code)

        //Check if himself can del
        assert (403 == UserAPI.delete(user1.id,USERNAME1,PASSWORD1).code)
    }

    void testUserSecurityForAnotherUser() {
        //Get user 1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

        //Get user 2
        User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

        //Check if another user can read/add/update/del
        assert (200 == UserAPI.create(BasicInstanceBuilder.getUserNotExist().encodeAsJSON(),USERNAME2,PASSWORD2).code)
        assert (200 == UserAPI.show(user1.id,USERNAME2,PASSWORD2).code)
        assert (403 == UserAPI.keys(user1.username,USERNAME2, PASSWORD2).code)
        assert (true ==UserAPI.containsInJSONList(user1.id,JSON.parse(UserAPI.list(USERNAME2,PASSWORD2).data)))
        assert (403 == UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAME2,PASSWORD2).code)

        //check if another user can add/del user from project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        assert (403 == ProjectAPI.addUserProject(project.id,user1.id,USERNAME2,PASSWORD2).code)
        assert (403 == ProjectAPI.deleteUserProject(project.id,user1.id,USERNAME2,PASSWORD2).code)

        //Check if another user can del
        assert (403 == UserAPI.delete(user1.id,USERNAME2,PASSWORD2).code)
    }

    void testUserSecurityForNotConnectedUser() {

        //Check if a non connected user can read/add/update/del
        assert (401 == UserAPI.create(BasicInstanceBuilder.getUserNotExist().encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
        assert (401 == UserAPI.show(user1.id,USERNAMEBAD,PASSWORDBAD).code)
        assert (401 == UserAPI.keys(user1.username,USERNAMEBAD, PASSWORDBAD).code)
        assert (401 == UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)

        //check if a non connected user  can add/del user from project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        assert (401 == ProjectAPI.addUserProject(project.id,user1.id,USERNAMEBAD,PASSWORDBAD).code)
        assert (401 == ProjectAPI.deleteUserProject(project.id,user1.id,USERNAMEBAD,PASSWORDBAD).code)

        //Check if a non connected user  can del
        assert (401 == UserAPI.delete(user1.id,USERNAMEBAD,PASSWORDBAD).code)
    }

}
