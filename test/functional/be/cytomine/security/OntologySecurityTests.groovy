package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import be.cytomine.utils.BasicInstance
import grails.converters.JSON
import be.cytomine.ontology.Ontology
import be.cytomine.test.http.OntologyAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class OntologySecurityTests extends functionaltestplugin.FunctionalTestCase {

    /**
     * Security test
     */
    static String USERNAME1 = "USERNAME1"
    static String PASSWORD1 = "PASSWORD1"
    static String USERNAME2 = "USERNAME2"
    static String PASSWORD2 = "PASSWORD2"
    static String USERNAME3 = "USERNAME3"
    static String PASSWORD3 = "PASSWORD3"
    static String USERNAMEADMIN = "USERNAMEADMIN"
    static String PASSWORDADMIN = "PASSWORDADMIN"
    static String USERNAMEBAD = "BADUSER"
    static String PASSWORDBAD = "BADPASSWORD"

  void testOntologySecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //Create new ontology (user1)
      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      Ontology ontology = result.data
      println "ontology="+ontology
      println "ontology.id="+ontology.id
      Infos.printRight(ontology)
      Infos.printUserRight(user1)
      Infos.printUserRight(admin)
      //check if admin user can access/update/delete
      assertEquals(200, OntologyAPI.show(ontology.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assertTrue(OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
      assertEquals(200, OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
      assertEquals(200, OntologyAPI.delete(ontology.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testOntologySecurityForOntologyCreator() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Create new Ontology (user1)
      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      Ontology ontology = result.data

      //check if user 1 can access/update/delete
      assertEquals(200, OntologyAPI.show(ontology.id,USERNAME1,PASSWORD1).code)
      assertTrue(OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAME1,PASSWORD1).data)))
      assertEquals(200, OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAME1,PASSWORD1).code)
      assertEquals(200, OntologyAPI.delete(ontology.id,USERNAME1,PASSWORD1).code)
  }

  void testOntologySecurityForProjectUser() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

      Ontology ontologyToAdd = BasicInstance.getBasicOntologyNotExist()

      //Create new Ontology (user1)
      def result = OntologyAPI.create(ontologyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      Ontology ontology = result.data

      Project project = BasicInstance.createBasicProjectNotExist()
      project.ontology = ontology
      BasicInstance.saveDomain(project)

      //TODO: try with USERNAME1 & PASSWORD1
      def resAddUser = ProjectAPI.addAdminProject(project.id,user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, resAddUser.code)
      resAddUser = ProjectAPI.addUserProject(project.id,user2.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, resAddUser.code)
      Infos.printRight(ontology)
      //check if user 2 can access/update/delete
      assertEquals(200, OntologyAPI.show(ontology.id,USERNAME2,PASSWORD2).code)
      assertTrue(OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAME2,PASSWORD2).data)))
      assertEquals(403, OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAME2,PASSWORD2).code)


      //remove right to user2
      resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
      assertEquals(200, resAddUser.code)

      Infos.printRight(ontology)
      //check if user 2 cannot access/update/delete
      assertEquals(403, OntologyAPI.show(ontology.id,USERNAME2,PASSWORD2).code)
      assertFalse(OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAME2,PASSWORD2).data)))
      assertEquals(403, OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAME2,PASSWORD2).code)

      //delete project because we will try to delete ontology
      def resDelProj = ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, resDelProj.code)


      assertEquals(403, OntologyAPI.delete(ontology.id,USERNAME2,PASSWORD2).code)
  }

  void testOntologySecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

      //Create new Ontology (user1)
      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      Ontology ontology = result.data
      Infos.printRight(ontology)
      //check if user 2 cannot access/update/delete
      assertEquals(403, OntologyAPI.show(ontology.id,USERNAME2,PASSWORD2).code)
      assertFalse(OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAME2,PASSWORD2).data)))
      assertEquals(403, OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assertEquals(403, OntologyAPI.delete(ontology.id,USERNAME2,PASSWORD2).code)

  }

  void testOntologySecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Create new Ontology (user1)
      def result = OntologyAPI.create(BasicInstance.getBasicOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      Ontology ontology = result.data
      Infos.printRight(ontology)
      //check if user 2 cannot access/update/delete
      assertEquals(401, OntologyAPI.show(ontology.id,USERNAMEBAD,PASSWORDBAD).code)
      assertEquals(401, OntologyAPI.list(USERNAMEBAD,PASSWORDBAD).code)
      assertEquals(401, OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
      assertEquals(401, OntologyAPI.delete(ontology.id,USERNAMEBAD,PASSWORDBAD).code)
  }
}
