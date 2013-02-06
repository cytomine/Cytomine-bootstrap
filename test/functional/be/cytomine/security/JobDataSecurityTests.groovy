package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.test.Infos

import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.BasicInstance
import grails.converters.JSON
import be.cytomine.processing.JobData
import be.cytomine.test.http.JobDataAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class JobDataSecurityTests extends SecurityTestsAbstract{


  void testJobDataSecurityForCytomineAdmin() {

      //Get user1
      User user1 = getUser1()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //Add jobData instance to project
      JobData jobData = BasicInstance.getBasicJobDataNotExist()
      jobData.job.project = project

      //check if admin user can access/update/delete
      result = JobDataAPI.create(jobData.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assertEquals(200, result.code)
      jobData = result.data
      assertEquals(200, JobDataAPI.show(jobData.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      result = JobDataAPI.listByJob(jobData.job.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assertEquals(200, result.code)
      assertTrue(JobDataAPI.containsInJSONList(jobData.id,JSON.parse(result.data)))
      assertEquals(200, JobDataAPI.update(jobData.id,jobData.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      assertEquals(200, JobDataAPI.delete(jobData.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
  }

  void testJobDataSecurityForProjectUser() {

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

      //Add jobData instance to project
      JobData jobData = BasicInstance.getBasicJobDataNotExist()
      jobData.job.project = project
      jobData.job.save(flush: true)
      //check if user 2 can access/update/delete
      result = JobDataAPI.create(jobData.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assertEquals(200, result.code)
      jobData = result.data
      assertEquals(200, JobDataAPI.show(jobData.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      result = JobDataAPI.listByJob(jobData.job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assertEquals(200, result.code)
      assertTrue(JobDataAPI.containsInJSONList(jobData.id,JSON.parse(result.data)))
      //assertEquals(200, JobDataAPI.update(jobData,USERNAME2,PASSWORD2).code)
      assertEquals(200, JobDataAPI.delete(jobData.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

  void testJobDataSecurityForSimpleUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //Add jobData instance to project
      JobData jobData = BasicInstance.getBasicJobDataNotExist()
      jobData.job.project = project

      //check if simple user can access/update/delete
      result = JobDataAPI.create(jobData.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assertEquals(403, result.code)
      jobData = result.data

      jobData = BasicInstance.createOrGetBasicJobData()
      jobData.job.project = project
      jobData.job.save(flush:true)

      assertEquals(403, JobDataAPI.show(jobData.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      assertEquals(403,JobDataAPI.listByJob(jobData.job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      //assertEquals(403, JobDataAPI.update(jobData,USERNAME2,PASSWORD2).code)
      assertEquals(403, JobDataAPI.delete(jobData.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

}
