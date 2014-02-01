package be.cytomine.security

import be.cytomine.processing.JobData
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.JobDataAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

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
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //Add jobData instance to project
      JobData jobData = BasicInstanceBuilder.getJobDataNotExist()
      jobData.job.project = project

      //check if admin user can access/update/delete
      result = JobDataAPI.create(jobData.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assert 200 == result.code
      jobData = result.data
      assert (200 == JobDataAPI.show(jobData.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      result = JobDataAPI.listByJob(jobData.job.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assert 200 == result.code
      assert (true ==JobDataAPI.containsInJSONList(jobData.id,JSON.parse(result.data)))
      assert (200 == JobDataAPI.update(jobData.id,jobData.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      assert (200 == JobDataAPI.delete(jobData.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
  }

  void testJobDataSecurityForProjectUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //add right to user 2
      def resAddUser = ProjectAPI.addUserProject(project.id,user2.id,SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      Infos.printRight(project)
      assert 200 == resAddUser.code

      //Add jobData instance to project
      JobData jobData = BasicInstanceBuilder.getJobDataNotExist()
      jobData.job.project = project
      jobData.job.save(flush: true)
      //check if user 2 can access/update/delete
      result = JobDataAPI.create(jobData.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assert 200 == result.code
      jobData = result.data
      assert (200 == JobDataAPI.show(jobData.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      result = JobDataAPI.listByJob(jobData.job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assert 200 == result.code
      assert (true ==JobDataAPI.containsInJSONList(jobData.id,JSON.parse(result.data)))
      //assert (200 == JobDataAPI.update(jobData,USERNAME2,PASSWORD2).code)
      assert (200 == JobDataAPI.delete(jobData.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

  void testJobDataSecurityForSimpleUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //Add jobData instance to project
      JobData jobData = BasicInstanceBuilder.getJobDataNotExist()
      jobData.job.project = project

      //check if simple user can access/update/delete
      result = JobDataAPI.create(jobData.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assert (403 == result.code)
      jobData = result.data

      jobData = BasicInstanceBuilder.getJobData()
      jobData.job.project = project
      jobData.job.save(flush:true)

      assert (403 == JobDataAPI.show(jobData.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      assert (403 ==JobDataAPI.listByJob(jobData.job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      //assert (403 == JobDataAPI.update(jobData,USERNAME2,PASSWORD2).code)
      assert (403 == JobDataAPI.delete(jobData.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

}
