package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class ProjectSecurityTests extends SecurityTestsAbstract {


  void testProjectSecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data
       Infos.printRight(project)
      Infos.printUserRight(user1)
       Infos.printUserRight(admin)
      //check if admin user can access/update/delete
      assert (200 == ProjectAPI.show(project.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assert (true ==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
      assert (200 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
      assert (200 == ProjectAPI.delete(project.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testProjectSecurityForProjectCreator() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //check if user 1 can access/update/delete
      assert (200 == ProjectAPI.show(project.id,USERNAME1,PASSWORD1).code)
      assert (true ==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAME1,PASSWORD1).data)))
      assert (200 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAME1,PASSWORD1).code)
      assert (200 == ProjectAPI.delete(project.id,USERNAME1,PASSWORD1).code)
  }

  void testProjectSecurityForProjectUser() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //Add right to user2
      def resAddUser = ProjectAPI.addUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
      assert 200 == resAddUser.code
      //log.info "AFTER:"+user2.getAuthorities().toString()

      Infos.printRight(project)
      //check if user 2 can access/update/delete
      assert (200 == ProjectAPI.show(project.id,USERNAME2,PASSWORD2).code)
      assert (true ==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAME2,PASSWORD2).data)))
      assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == ProjectAPI.delete(project.id,USERNAME2,PASSWORD2).code)


      //remove right to user2
      resAddUser = ProjectAPI.deleteUserProject(project.id,user2.id,USERNAME1,PASSWORD1)
      assert 200 == resAddUser.code

      Infos.printRight(project)
      //check if user 2 cannot access/update/delete
      assert (403 == ProjectAPI.show(project.id,USERNAME2,PASSWORD2).code)
      assert (false == ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAME2,PASSWORD2).data)))
      assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == ProjectAPI.delete(project.id,USERNAME2,PASSWORD2).code)
  }

  void testProjectSecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data
      Infos.printRight(project)
      //check if user 2 cannot access/update/delete
      assert (403 == ProjectAPI.show(project.id,USERNAME2,PASSWORD2).code)
      assert(false==ProjectAPI.containsInJSONList(project.id,JSON.parse(ProjectAPI.list(USERNAME2,PASSWORD2).data)))
      Infos.printRight(project)
      assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == ProjectAPI.delete(project.id,USERNAME2,PASSWORD2).code)

  }

  void testProjectSecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Project project = result.data
      Infos.printRight(project)
      //check if user 2 cannot access/update/delete
      assert (401 == ProjectAPI.show(project.id,USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == ProjectAPI.list(USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == ProjectAPI.update(project.id,project.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == ProjectAPI.delete(project.id,USERNAMEBAD,PASSWORDBAD).code)
  }

  void testAddProjectGrantAdminUndoRedo() {
    //not implemented (no undo/redo for project)
  }
}
