package be.cytomine.security

import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.OntologyAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class OntologySecurityTests extends SecurityTestsAbstract {


  void testOntologySecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstanceBuilder.getSuperAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //Create new ontology (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Ontology ontology = result.data
      println "ontology="+ontology
      println "ontology.id="+ontology.id
      Infos.printRight(ontology)
      Infos.printUserRight(user1)
      Infos.printUserRight(admin)
      //check if admin user can access/update/delete
      assert (200 == OntologyAPI.show(ontology.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assert (true ==OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
      assert (200 == OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
      assert (200 == OntologyAPI.delete(ontology.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testOntologySecurityForOntologyCreator() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new Ontology (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Ontology ontology = result.data

      //check if user 1 can access/update/delete
      assert (200 == OntologyAPI.show(ontology.id,USERNAME1,PASSWORD1).code)
      assert (true ==OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAME1,PASSWORD1).data)))
      assert (200 == OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAME1,PASSWORD1).code)
      assert (200 == OntologyAPI.delete(ontology.id,USERNAME1,PASSWORD1).code)
  }

  void testOntologySecurityForProjectUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      Ontology ontologyToAdd = BasicInstanceBuilder.getOntologyNotExist()

      //Create new Ontology (user1)
      def result = OntologyAPI.create(ontologyToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Ontology ontology = result.data

      Project project = BasicInstanceBuilder.getProjectNotExist(true)
      project.ontology = ontology
      BasicInstanceBuilder.saveDomain(project)

      //TODO: try with USERNAME1 & PASSWORD1
      def resAddUser = ProjectAPI.addAdminProject(project.id,user1.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 == resAddUser.code
      resAddUser = ProjectAPI.addUserProject(project.id,user2.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 == resAddUser.code
      Infos.printRight(ontology)
      //check if user 2 can access/update/delete
      assert (200 == OntologyAPI.show(ontology.id,USERNAME2,PASSWORD2).code)
      assert (true ==OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAME2,PASSWORD2).data)))
      assert (403 == OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAME2,PASSWORD2).code)


      //remove right to user2
      resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
      assert 200 == resAddUser.code

      Infos.printRight(ontology)
      //check if user 2 cannot access/update/delete
      assert (403 == OntologyAPI.show(ontology.id,USERNAME2,PASSWORD2).code)
      assert(false==OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAME2,PASSWORD2).data)))
      assert (403 == OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAME2,PASSWORD2).code)

      //delete project because we will try to delete ontology
      def resDelProj = ProjectAPI.delete(project.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert (200 == resDelProj.code)


      assert (403 == OntologyAPI.delete(ontology.id,USERNAME2,PASSWORD2).code)
  }

    //
    void testOntologySecurityForGhest() {

        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
        //Get user2
        User ghest = BasicInstanceBuilder.getGhest("GHESTONTOLOGY","PASSWORD")

        //Create new Ontology (user1)
        def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Ontology ontology = result.data
        Infos.printRight(ontology)
        //check if user 2 cannot access/update/delete
        assert (403 == OntologyAPI.show(ontology.id,"GHESTONTOLOGY","PASSWORD").code)
        assert (false==OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list("GHESTONTOLOGY","PASSWORD").data)))
        assert (403 == OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),"GHESTONTOLOGY","PASSWORD").code)
        assert (403 == OntologyAPI.delete(ontology.id,"GHESTONTOLOGY","PASSWORD").code)
        assert (403 == OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),"GHESTONTOLOGY","PASSWORD").code)

    }


  void testOntologySecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      //Create new Ontology (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Ontology ontology = result.data
      Infos.printRight(ontology)
      //check if user 2 cannot access/update/delete
      assert (403 == OntologyAPI.show(ontology.id,USERNAME2,PASSWORD2).code)
      assert (false==OntologyAPI.containsInJSONList(ontology.id,JSON.parse(OntologyAPI.list(USERNAME2,PASSWORD2).data)))
      assert (403 == OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == OntologyAPI.delete(ontology.id,USERNAME2,PASSWORD2).code)

  }

  void testOntologySecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new Ontology (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Ontology ontology = result.data
      Infos.printRight(ontology)
      //check if user 2 cannot access/update/delete
      assert (401 == OntologyAPI.show(ontology.id,USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == OntologyAPI.list(USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == OntologyAPI.update(ontology.id,ontology.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == OntologyAPI.delete(ontology.id,USERNAMEBAD,PASSWORDBAD).code)
  }
}
