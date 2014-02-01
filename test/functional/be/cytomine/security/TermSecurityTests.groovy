package be.cytomine.security

import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.OntologyAPI
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.TermAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class TermSecurityTests extends SecurityTestsAbstract {


  void testTermSecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstanceBuilder.getAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //Create new term (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def termToAdd = BasicInstanceBuilder.getTermNotExist()
      termToAdd.ontology = result.data
      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Term term = result.data
      println "term="+term
      println "term.id="+term.id
      //check if admin user can access/update/delete
      assert (200 == TermAPI.show(term.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assert (true ==TermAPI.containsInJSONList(term.id,JSON.parse(TermAPI.listByOntology(term.ontology.id,USERNAMEADMIN,PASSWORDADMIN).data)))
      assert (200 == TermAPI.update(term.id,term.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
      assert (200 == TermAPI.delete(term.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testTermSecurityForOntologyCreator() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new Term (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def termToAdd = BasicInstanceBuilder.getTermNotExist()
      termToAdd.ontology = result.data
      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Term term = result.data

      //check if user 1 can access/update/delete
      assert (200 == TermAPI.show(term.id,USERNAME1,PASSWORD1).code)
      assert (true ==TermAPI.containsInJSONList(term.id,JSON.parse(TermAPI.listByOntology(term.ontology.id,USERNAME1,PASSWORD1).data)))
      assert (200 == TermAPI.update(term.id,term.encodeAsJSON(),USERNAME1,PASSWORD1).code)
      assert (200 == TermAPI.delete(term.id,USERNAME1,PASSWORD1).code)
  }

  void testTermSecurityForOntologyUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      //Create new Term (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def termToAdd = BasicInstanceBuilder.getTermNotExist()
      termToAdd.ontology = result.data
      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def term = result.data

      Project project = BasicInstanceBuilder.getProjectNotExist(true)
      project.ontology = termToAdd.ontology
      BasicInstanceBuilder.saveDomain(project)

      //TODO: try with USERNAME1 & PASSWORD1
      def resAddUser = ProjectAPI.addAdminProject(project.id,user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == resAddUser.code
      resAddUser = ProjectAPI.addUserProject(project.id,user2.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == resAddUser.code
      //check if user 2 can access/update/delete
      assert (200 == TermAPI.show(term.id,USERNAME2,PASSWORD2).code)
      assert (true ==TermAPI.containsInJSONList(term.id,JSON.parse(TermAPI.listByOntology(term.ontology.id,USERNAME2,PASSWORD2).data)))
      assert (403 == TermAPI.update(term.id,term.encodeAsJSON(),USERNAME2,PASSWORD2).code)


      //remove right to user2
      resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
      assert 200 == resAddUser.code
      //check if user 2 cannot access/update/delete
      assert (403 == TermAPI.show(term.id,USERNAME2,PASSWORD2).code)
      assert (403 == TermAPI.listByOntology(term.ontology.id,USERNAME2,PASSWORD2).code)
      assert (403 == TermAPI.update(term.id,term.encodeAsJSON(),USERNAME2,PASSWORD2).code)

      //delete project because we will try to delete term
      def resDelProj = ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert (200 == resDelProj.code)


      assert (403 == TermAPI.delete(term.id,USERNAME2,PASSWORD2).code)
  }

  void testTermSecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      //Create new Term (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def termToAdd = BasicInstanceBuilder.getTermNotExist()
      termToAdd.ontology = result.data
      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Term term = result.data

      //check if user 2 cannot access/update/delete
      assert (403 == TermAPI.show(term.id,USERNAME2,PASSWORD2).code)
      assert (403 == TermAPI.update(term.id,term.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == TermAPI.delete(term.id,USERNAME2,PASSWORD2).code)

  }

  void testTermSecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new Term (user1)
      def result = OntologyAPI.create(BasicInstanceBuilder.getOntologyNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def termToAdd = BasicInstanceBuilder.getTermNotExist()
      termToAdd.ontology = result.data
      result = TermAPI.create(termToAdd.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Term term = result.data
      //check if user 2 cannot access/update/delete
      assert (401 == TermAPI.show(term.id,USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == TermAPI.list(USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == TermAPI.update(term.id,term.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == TermAPI.delete(term.id,USERNAMEBAD,PASSWORDBAD).code)
  }
}
