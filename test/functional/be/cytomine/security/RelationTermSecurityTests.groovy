package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.test.Infos

import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.BasicInstance
import grails.converters.JSON
import be.cytomine.test.http.RelationTermAPI
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term

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
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //Create new relationterm (user1)
      def rel = BasicInstance.getBasicRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
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
      assertEquals(200, RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assertTrue(RelationTermAPI.containsInJSONList(relationterm.id,JSON.parse(RelationTermAPI.listByTermAll(relationterm.term1.id,USERNAMEADMIN,PASSWORDADMIN).data)))
      assertEquals(200, RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testRelationTermSecurityForOntologyCreator() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Create new RelationTerm (user1)
      def rel = BasicInstance.getBasicRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"

      //check if user 1 can access/update/delete
      assertEquals(200, RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME1,PASSWORD1).code)
      assertTrue(RelationTermAPI.containsInJSONList(relationterm.id,JSON.parse(RelationTermAPI.listByTermAll(relationterm.term1.id,USERNAME1,PASSWORD1).data)))
      assertEquals(200, RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME1,PASSWORD1).code)
  }

  void testRelationTermSecurityForProjectUser() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

      //Create new RelationTerm (user1)
      def rel = BasicInstance.getBasicRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"

      Project project = BasicInstance.createBasicProjectNotExist()
      project.ontology = relationterm.term1.ontology
      BasicInstance.saveDomain(project)

      //TODO: try with USERNAME1 & PASSWORD1
      def resAddUser = ProjectAPI.addAdminProject(project.id,user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, resAddUser.code)
      resAddUser = ProjectAPI.addUserProject(project.id,user2.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, resAddUser.code)
      Infos.printRight(relationterm)
      //check if user 2 can access/update/delete
      assertEquals(200, RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)
      assertTrue(RelationTermAPI.containsInJSONList(relationterm.id,JSON.parse(RelationTermAPI.listByTermAll(relationterm.term1.id,USERNAME2,PASSWORD2).data)))


      //remove right to user2
      resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, resAddUser.code)

      Infos.printRight(relationterm)
      //check if user 2 cannot access/update/delete
      assertEquals(403, RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)

      //delete project because we will try to delete relationterm
      def resDelProj = ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, resDelProj.code)


      assertEquals(403, RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)
  }

  void testRelationTermSecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

      //Create new RelationTerm (user1)
      def rel = BasicInstance.getBasicRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"
      //check if user 2 cannot access/update/delete
      assertEquals(403, RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)
      assertEquals(403, RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAME2,PASSWORD2).code)

  }

  void testRelationTermSecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Create new RelationTerm (user1)
      def rel = BasicInstance.getBasicRelationTermNotExist()
      Infos.addUserRight(user1,rel.term1.ontology)
      def result = RelationTermAPI.create(rel.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      RelationTerm relationterm = RelationTerm.findWhere('relation': Relation.read(json.relationterm.relation), 'term1': Term.read(json.relationterm.term1), 'term2': Term.read(json.relationterm.term2))
      println "relationterm=${relationterm}"
      //check if user 2 cannot access/update/delete
      assertEquals(401, RelationTermAPI.show(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAMEBAD,PASSWORDBAD).code)
      assertEquals(401, RelationTermAPI.listByTermAll(relationterm.term1.id,USERNAMEBAD,PASSWORDBAD).code)
      assertEquals(401, RelationTermAPI.delete(relationterm.relation.id, relationterm.term1.id, relationterm.term2.id,USERNAMEBAD,PASSWORDBAD).code)
  }
}
