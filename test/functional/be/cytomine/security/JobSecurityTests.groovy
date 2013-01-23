package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.ProjectAPI
import be.cytomine.utils.BasicInstance
import grails.converters.JSON
import be.cytomine.processing.Job
import be.cytomine.test.http.JobAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class JobSecurityTests extends SecurityTestsAbstract{


  void testJobSecurityForCytomineAdmin() {

      //Get user1
      User user1 = getUser1()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //Add job instance to project
      Job job = BasicInstance.getBasicJobNotExist()
      job.project = project

      //check if admin user can access/update/delete
      result = JobAPI.create(job.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assertEquals(200, result.code)
      job = result.data
      assertEquals(200, JobAPI.show(job.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      result = JobAPI.listBySoftwareAndProject(job.software.id,project.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN,true)
      assertEquals(200, result.code)
      assertTrue(JobAPI.containsInJSONList(job.id,JSON.parse(result.data)))
      assertEquals(200, JobAPI.update(job.id,job.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      assertEquals(200, JobAPI.delete(job.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
  }

  void testJobSecurityForProjectUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //add right to user 2
      def resAddUser = ProjectAPI.addUserProject(project.id,user2.id,SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      Infos.printRight(project)
      assertEquals(200, resAddUser.code)

      //Add job instance to project
      Job job = BasicInstance.getBasicJobNotExist()
      job.project = project

      //check if user 2 can access/update/delete
      result = JobAPI.create(job.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assertEquals(200, result.code)
      job = result.data
      assertEquals(200, JobAPI.show(job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      result = JobAPI.listBySoftwareAndProject(job.software.id,project.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2,true)
      assertEquals(200, result.code)
      assertTrue(JobAPI.containsInJSONList(job.id,JSON.parse(result.data)))
      //assertEquals(200, JobAPI.update(job,USERNAME2,PASSWORD2).code)
      assertEquals(200, JobAPI.delete(job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

  void testJobSecurityForSimpleUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //Add job instance to project
      Job job = BasicInstance.getBasicJobNotExist()
      job.project = project

      //check if simple user can access/update/delete
      result = JobAPI.create(job.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assertEquals(403, result.code)
      job = result.data

      job = BasicInstance.createOrGetBasicJob()
      job.project = project
      job.save(flush:true)

      assertEquals(403, JobAPI.show(job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      assertEquals(403,JobAPI.listBySoftwareAndProject(job.software.id,project.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2,true).code)
      //assertEquals(403, JobAPI.update(job,USERNAME2,PASSWORD2).code)
      assertEquals(403, JobAPI.delete(job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

}
