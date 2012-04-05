package be.cytomine.security

import be.cytomine.project.Project

import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import be.cytomine.image.ImageInstance
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.SecurityTestsAbstract

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class ImageInstanceSecurityTests extends SecurityTestsAbstract{


  void testImageInstanceSecurityForCytomineAdmin() {

      //Get user1
      User user1 = getUser1()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist(),be.cytomine.SecurityTestsAbstract.USERNAME1,be.cytomine.SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //Add image instance to project
      ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
      image.project = project
      //check if admin user can access/update/delete
      result = ImageInstanceAPI.create(image,be.cytomine.SecurityTestsAbstract.USERNAMEADMIN,be.cytomine.SecurityTestsAbstract.PASSWORDADMIN)
      assertEquals(200, result.code)
      image = result.data
      assertEquals(200, ImageInstanceAPI.show(image.id,be.cytomine.SecurityTestsAbstract.USERNAMEADMIN,be.cytomine.SecurityTestsAbstract.PASSWORDADMIN).code)
      result = ImageInstanceAPI.listByProject(project.id,be.cytomine.SecurityTestsAbstract.USERNAMEADMIN,be.cytomine.SecurityTestsAbstract.PASSWORDADMIN)
      assertEquals(200, result.code)
      assertTrue(ImageInstanceAPI.containsInJSONList(image.id,JSON.parse(result.data)))
      assertEquals(200, ImageInstanceAPI.update(image,be.cytomine.SecurityTestsAbstract.USERNAMEADMIN,be.cytomine.SecurityTestsAbstract.PASSWORDADMIN).code)
      assertEquals(200, ImageInstanceAPI.delete(image,be.cytomine.SecurityTestsAbstract.USERNAMEADMIN,be.cytomine.SecurityTestsAbstract.PASSWORDADMIN).code)
  }

  void testImageInstanceSecurityForProjectUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist(),be.cytomine.SecurityTestsAbstract.USERNAME1,be.cytomine.SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data
      def resAddUser = ProjectAPI.addUserProject(project.id,user2.id,be.cytomine.SecurityTestsAbstract.USERNAME1,be.cytomine.SecurityTestsAbstract.PASSWORD1)
      Infos.printRight(project)
      assertEquals(200, resAddUser.code)
      //Add image instance to project
      ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
      image.project = project
      //check if admin user can access/update/delete
      result = ImageInstanceAPI.create(image,be.cytomine.SecurityTestsAbstract.USERNAME2,be.cytomine.SecurityTestsAbstract.PASSWORD2)
      assertEquals(200, result.code)
      image = result.data
      assertEquals(200, ImageInstanceAPI.show(image.id,be.cytomine.SecurityTestsAbstract.USERNAME2,be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
      result = ImageInstanceAPI.listByProject(project.id,be.cytomine.SecurityTestsAbstract.USERNAME2,be.cytomine.SecurityTestsAbstract.PASSWORD2)
      assertEquals(200, result.code)
      assertTrue(ImageInstanceAPI.containsInJSONList(image.id,JSON.parse(result.data)))
      //assertEquals(200, ImageInstanceAPI.update(image,USERNAME2,PASSWORD2).code)
      assertEquals(200, ImageInstanceAPI.delete(image,be.cytomine.SecurityTestsAbstract.USERNAME2,be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
  }

  void testImageInstanceSecurityForSimpleUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist(),be.cytomine.SecurityTestsAbstract.USERNAME1,be.cytomine.SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data
      //Add image instance to project
      ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
      image.project = project
      //check if admin user can access/update/delete
      result = ImageInstanceAPI.create(image,be.cytomine.SecurityTestsAbstract.USERNAME2,be.cytomine.SecurityTestsAbstract.PASSWORD2)
      assertEquals(403, result.code)
      image = result.data

      image = BasicInstance.createOrGetBasicImageInstance()
      image.project = project
      image.save(flush:true)

      assertEquals(403, ImageInstanceAPI.show(image.id,be.cytomine.SecurityTestsAbstract.USERNAME2,be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
      assertEquals(403,ImageInstanceAPI.listByProject(project.id,be.cytomine.SecurityTestsAbstract.USERNAME2,be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
      //assertEquals(403, ImageInstanceAPI.update(image,USERNAME2,PASSWORD2).code)
      assertEquals(403, ImageInstanceAPI.delete(image,be.cytomine.SecurityTestsAbstract.USERNAME2,be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
  }

}
