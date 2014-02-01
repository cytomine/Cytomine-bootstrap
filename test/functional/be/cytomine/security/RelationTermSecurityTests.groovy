package be.cytomine.security

import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.RelationTermAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class RelationTermSecurityTests extends SecurityTestsAbstract {


  void testRelationTermSecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstanceBuilder.getAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //Create new relationterm (user1)
      def rel = BasicInstanceBuilder.getRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def json = JSON.parse(result.data)

      println "result=${result.data}"
      println "result.data=${result.data}"
      println "JSON.parse(result.data)=${JSON.parse(result.data)}"

      println "relation=${json.relationterm.relation}"
      println "term1=${json.relationterm.term1}"
      println "term2=${json.relationterm.term2}"

      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"
      //check if admin user can access/update/delete
      assert (200 == RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assert (true ==RelationTermAPI.containsInJSONList(relationterm.id,JSON.parse(RelationTermAPI.listByTermAll(relationterm.term1.id,USERNAMEADMIN,PASSWORDADMIN).data)))
      assert (200 == RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testRelationTermSecurityForOntologyCreator() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new RelationTerm (user1)
      def rel = BasicInstanceBuilder.getRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"

      //check if user 1 can access/update/delete
      assert (200 == RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME1,PASSWORD1).code)
      assert (true ==RelationTermAPI.containsInJSONList(relationterm.id,JSON.parse(RelationTermAPI.listByTermAll(relationterm.term1.id,USERNAME1,PASSWORD1).data)))
      assert (200 == RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME1,PASSWORD1).code)
  }

  void testRelationTermSecurityForProjectUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      //Create new RelationTerm (user1)
      def rel = BasicInstanceBuilder.getRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"

      Project project = BasicInstanceBuilder.getProjectNotExist(true)
      project.ontology = relationterm.term1.ontology
      BasicInstanceBuilder.saveDomain(project)

      //TODO: try with USERNAME1 & PASSWORD1
      def resAddUser = ProjectAPI.addAdminProject(project.id,user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == resAddUser.code
      resAddUser = ProjectAPI.addUserProject(project.id,user2.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == resAddUser.code
      Infos.printRight(relationterm)
      //check if user 2 can access/update/delete
      assert (200 == RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)
      assert (true ==RelationTermAPI.containsInJSONList(relationterm.id,JSON.parse(RelationTermAPI.listByTermAll(relationterm.term1.id,USERNAME2,PASSWORD2).data)))


      //remove right to user2
      resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == resAddUser.code

      Infos.printRight(relationterm)
      //check if user 2 cannot access/update/delete
      assert (403 == RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)

      //delete project because we will try to delete relationterm
      def resDelProj = ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert (200 == resDelProj.code)


      assert (403 == RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)
  }

  void testRelationTermSecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      //Create new RelationTerm (user1)
      def rel = BasicInstanceBuilder.getRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"
      //check if user 2 cannot access/update/delete
      assert (403 == RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)
      assert (403 == RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)

  }

  void testRelationTermSecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new RelationTerm (user1)
      def rel = BasicInstanceBuilder.getRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"
      //check if user 2 cannot access/update/delete
      assert (401 == RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == RelationTermAPI.listByTermAll(relationterm.term1.id,USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAMEBAD,PASSWORDBAD).code)
  }
}
