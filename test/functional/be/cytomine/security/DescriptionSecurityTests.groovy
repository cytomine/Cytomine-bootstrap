package be.cytomine.security

import be.cytomine.utils.Description
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.DescriptionAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class DescriptionSecurityTests extends SecurityTestsAbstract{


  void testDescriptionSecurityForCytomineAdmin() {

      //Get user1
      User user1 = getUser1()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //Add description instance to project
      Description description = BasicInstanceBuilder.getDescriptionNotExist(project,false)
      description.setDomain(project)
      //check if admin user can access/update/delete
      result = DescriptionAPI.create(project.id,project.class.name,description.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assert 200 == result.code
      description = result.data
      assert (200 == DescriptionAPI.show(project.id,project.class.name,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      assert (200 == DescriptionAPI.update(project.id,project.class.name,description.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      assert (200 == DescriptionAPI.delete(project.id,project.class.name,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
  }

  void testDescriptionSecurityForProjectUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data
      def resAddUser = ProjectAPI.addUserProject(project.id,user2.id,SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      Infos.printRight(project)
      assert 200 == resAddUser.code

      //Add description instance to project
      Description description = BasicInstanceBuilder.getDescriptionNotExist(project, false)
      description.setDomain(project)

      //check if user 2 can access/update/delete
      result = DescriptionAPI.create(project.id,project.class.name,description.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assert 200 == result.code
      description = result.data
      assert (200 == DescriptionAPI.show(project.id,project.class.name,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      assert (200 == DescriptionAPI.update(project.id,project.class.name,description.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (200 == DescriptionAPI.delete(project.id,project.class.name,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

  void testDescriptionSecurityForSimpleUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //Add description instance to project
      Description description = BasicInstanceBuilder.getDescriptionNotExist(project, false)
      description.setDomain(project)

      //check if simple  user can access/update/delete
      result = DescriptionAPI.create(project.id,project.class.name,description.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assert (403 == result.code)
      description = result.data

      description = BasicInstanceBuilder.getDescriptionNotExist(project,false)
      description.setDomain(project)
      description.save(flush:true)

      assert (403 == DescriptionAPI.show(project.id,project.class.name,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

}
