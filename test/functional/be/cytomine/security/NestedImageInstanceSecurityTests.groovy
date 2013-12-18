package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.image.NestedImageInstance
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.NestedImageInstanceAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class NestedImageInstanceSecurityTests extends SecurityTestsAbstract{


  void testNestedImageInstanceSecurityForCytomineAdmin() {

      //Get user1
      User user1 = getUser1()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //Add image instance to project
      NestedImageInstance image = BasicInstanceBuilder.getNestedImageInstanceNotExist()
      image.project = project
      //check if admin user can access/update/delete
      result = NestedImageInstanceAPI.create(image.parent.id,image.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assert 200 == result.code
      image = result.data
      assert (200 == NestedImageInstanceAPI.show(image.id,image.parent.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      result = NestedImageInstanceAPI.listByImageInstance(image.parent.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assert 200 == result.code
      assert (true ==NestedImageInstanceAPI.containsInJSONList(image.id,JSON.parse(result.data)))
      assert (200 == NestedImageInstanceAPI.update(image.id,image.parent.id,image.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      assert (200 == NestedImageInstanceAPI.delete(image.id,image.parent.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
  }

  void testNestedImageInstanceSecurityForProjectUser() {

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


      //Add image instance to project
      NestedImageInstance image = BasicInstanceBuilder.getNestedImageInstanceNotExist()
      image.project = project

      image.parent.project = project
      BasicInstanceBuilder.saveDomain(project)

      //check if user 2 can access/update/delete
      result = NestedImageInstanceAPI.create(image.parent.id,image.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assert 200 == result.code
      image = result.data
      assert (200 == NestedImageInstanceAPI.show(image.id,image.parent.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      result = NestedImageInstanceAPI.listByImageInstance(image.parent.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assert 200 == result.code
      assert (true ==NestedImageInstanceAPI.containsInJSONList(image.id,JSON.parse(result.data)))
      //assert (200 == NestedImageInstanceAPI.update(image,USERNAME2,PASSWORD2).code)
      assert (200 == NestedImageInstanceAPI.delete(image.id,image.parent.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

  void testNestedImageInstanceSecurityForSimpleUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //Add image instance to project
      NestedImageInstance image = BasicInstanceBuilder.getNestedImageInstanceNotExist()
      image.project = project

      //check if simple  user can access/update/delete
      result = NestedImageInstanceAPI.create(image.parent.id,image.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assert (403 == result.code)
      image = result.data

      image = BasicInstanceBuilder.getNestedImageInstance()
      image.project = project
      image.save(flush:true)

      assert (403 == NestedImageInstanceAPI.show(image.id,image.parent.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      assert (403 ==NestedImageInstanceAPI.listByImageInstance(image.parent.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      //assert (403 == NestedImageInstanceAPI.update(image,USERNAME2,PASSWORD2).code)
      assert (403 == NestedImageInstanceAPI.delete(image.id,image.parent.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

}
