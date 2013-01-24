package be.cytomine.security

import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.test.Infos
import be.cytomine.test.http.OntologyAPI
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.TermAPI
import be.cytomine.utils.BasicInstance
import grails.converters.JSON
import be.cytomine.test.http.UserAPI

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
        User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

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

        //Check if himself can read/add/update/del
        assertEquals(403, UserAPI.create(BasicInstance.getBasicUserNotExist().encodeAsJSON(),USERNAME2,PASSWORD2).code)
        assertEquals(200, UserAPI.show(user1.id,USERNAME2,PASSWORD2).code)
        assertTrue(UserAPI.containsInJSONList(user1.id,JSON.parse(UserAPI.list(USERNAME2,PASSWORD2).data)))
        assertEquals(403, UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAME2,PASSWORD2).code)

        //check if himself can add/del user from project
        Project project = BasicInstance.createBasicProjectNotExist()
        assertEquals(403, ProjectAPI.addUserProject(project.id,user1.id,USERNAME2,PASSWORD2).code)
        assertEquals(403, ProjectAPI.deleteUserProject(project.id,user1.id,USERNAME2,PASSWORD2).code)

        //Check if himself can del
        assertEquals(403, UserAPI.delete(user1.id,USERNAME2,PASSWORD2).code)
    }

    void testUserSecurityForNotConnectedUser() {

        //Check if himself can read/add/update/del
        assertEquals(401, UserAPI.create(BasicInstance.getBasicUserNotExist().encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
        assertEquals(401, UserAPI.show(user1.id,USERNAMEBAD,PASSWORDBAD).code)
        assertEquals(401, UserAPI.update(user1.id,user1.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)

        //check if himself can add/del user from project
        Project project = BasicInstance.createBasicProjectNotExist()
        assertEquals(401, ProjectAPI.addUserProject(project.id,user1.id,USERNAMEBAD,PASSWORDBAD).code)
        assertEquals(401, ProjectAPI.deleteUserProject(project.id,user1.id,USERNAMEBAD,PASSWORDBAD).code)

        //Check if himself can del
        assertEquals(401, UserAPI.delete(user1.id,USERNAMEBAD,PASSWORDBAD).code)
    }



//  void testTermSecurityForCytomineAdmin() {
//
//      //Get user1
//      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
//
//      //Get admin user
//      User admin = BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)
//
//      //Create new term (user1)
//      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      def termToAdd = BasicInstance.getBasicTermNotExist()
//      termToAdd.ontology = result.data
//      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      Term term = result.data
//      println "term="+term
//      println "term.id="+term.id
//      //check if admin user can access/update/delete
//      assertEquals(200, TermAPI.show(term.id,USERNAMEADMIN,PASSWORDADMIN).code)
//      assertTrue(TermAPI.containsInJSONList(term.id,JSON.parse(TermAPI.listByOntology(termToAdd.ontology.id,USERNAMEADMIN,PASSWORDADMIN).data)))
//      assertEquals(200, TermAPI.update(term.id,term.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
//      assertEquals(200, TermAPI.delete(term.id,USERNAMEADMIN,PASSWORDADMIN).code)
//  }
//
//  void testTermSecurityForTermCreator() {
//
//      //Get user1
//      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
//
//      //Create new Term (user1)
//      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      def termToAdd = BasicInstance.getBasicTermNotExist()
//      termToAdd.ontology = result.data
//      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      Term term = result.data
//
//      //check if user 1 can access/update/delete
//      assertEquals(200, TermAPI.show(term.id,USERNAME1,PASSWORD1).code)
//      assertTrue(TermAPI.containsInJSONList(term.id,JSON.parse(TermAPI.listByOntology(termToAdd.ontology.id,USERNAME1,PASSWORD1).data)))
//      assertEquals(200, TermAPI.update(term.id,term.encodeAsJSON(),USERNAME1,PASSWORD1).code)
//      assertEquals(200, TermAPI.delete(term.id,USERNAME1,PASSWORD1).code)
//  }
//
//  void testTermSecurityForProjectUser() {
//
//      //Get user1
//      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
//      //Get user2
//      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)
//
//      //Create new Term (user1)
//      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      def termToAdd = BasicInstance.getBasicTermNotExist()
//      termToAdd.ontology = result.data
//      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      def term = result.data
//
//      Project project = BasicInstance.createBasicProjectNotExist()
//      project.ontology = termToAdd.ontology
//      BasicInstance.saveDomain(project)
//
//      //TODO: try with USERNAME1 & PASSWORD1
//      def resAddUser = ProjectAPI.addAdminProject(project.id,user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//      assertEquals(200, resAddUser.code)
//      resAddUser = ProjectAPI.addUserProject(project.id,user2.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//      assertEquals(200, resAddUser.code)
//      //check if user 2 can access/update/delete
//      assertEquals(200, TermAPI.show(term.id,USERNAME2,PASSWORD2).code)
//      assertTrue(TermAPI.containsInJSONList(term.id,JSON.parse(TermAPI.listByOntology(termToAdd.ontology.id,USERNAME2,PASSWORD2).data)))
//      assertEquals(403, TermAPI.update(term.id,term.encodeAsJSON(),USERNAME2,PASSWORD2).code)
//
//
//      //remove right to user2
//      resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
//      assertEquals(200, resAddUser.code)
//      //check if user 2 cannot access/update/delete
//      assertEquals(403, TermAPI.show(term.id,USERNAME2,PASSWORD2).code)
//      assertEquals(403, TermAPI.listByOntology(termToAdd.ontology.id,USERNAME2,PASSWORD2).code)
//      assertEquals(403, TermAPI.update(term.id,term.encodeAsJSON(),USERNAME2,PASSWORD2).code)
//
//      //delete project because we will try to delete term
//      def resDelProj = ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//      assertEquals(200, resDelProj.code)
//
//
//      assertEquals(403, TermAPI.delete(term.id,USERNAME2,PASSWORD2).code)
//  }
//
//  void testTermSecurityForSimpleUser() {
//
//      //Get user1
//      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
//      //Get user2
//      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)
//
//      //Create new Term (user1)
//      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      def termToAdd = BasicInstance.getBasicTermNotExist()
//      termToAdd.ontology = result.data
//      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      Term term = result.data
//
//      //check if user 2 cannot access/update/delete
//      assertEquals(403, TermAPI.show(term.id,USERNAME2,PASSWORD2).code)
//      assertEquals(403, TermAPI.update(term.id,term.encodeAsJSON(),USERNAME2,PASSWORD2).code)
//      assertEquals(403, TermAPI.delete(term.id,USERNAME2,PASSWORD2).code)
//
//  }
//
//  void testTermSecurityForAnonymous() {
//
//      //Get user1
//      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
//
//      //Create new Term (user1)
//      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      def termToAdd = BasicInstance.getBasicTermNotExist()
//      termToAdd.ontology = result.data
//      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
//      assertEquals(200, result.code)
//      Term term = result.data
//      //check if user 2 cannot access/update/delete
//      assertEquals(401, TermAPI.show(term.id,USERNAMEBAD,PASSWORDBAD).code)
//      assertEquals(401, TermAPI.list(USERNAMEBAD,PASSWORDBAD).code)
//      assertEquals(401, TermAPI.update(term.id,term.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
//      assertEquals(401, TermAPI.delete(term.id,USERNAMEBAD,PASSWORDBAD).code)
//  }
}
