package be.cytomine.security

import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.JobAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class UserJobSecurityTests extends SecurityTestsAbstract {


    void testUserJobWorkflow() {
        //create basic user
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
        assert !user1.isAdmin()
        //create project
        def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(), Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data

        //add user in project
        def resAddUser = ProjectAPI.addUserProject(project.id,user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        Infos.printRight(project)
        assert 200 == resAddUser.code

        //create userjob
         log.info("create user")
         def parent = user1;
         def json = "{parent:"+ parent.id +", username:"+ Math.random()+", software: ${BasicInstanceBuilder.getSoftware().id}, project : ${project.id}}";

         log.info("post user child")
         String URL = Infos.CYTOMINEURL+"api/userJob.json"
         HttpClient client = new HttpClient()
         client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
         client.post(json.toString())
         int code  = client.getResponseCode()
         String response = client.getResponseData()
         println response
         client.disconnect();

         log.info("check response")
         assert 200==code
         json = JSON.parse(response)
         assert json instanceof JSONObject
        println "json=$json"
         UserJob userJob = UserJob.read(json.userJob.id)
         Job job = userJob.job
        userJob.username ="testJobWorkflow"
        userJob.password = "password"


        userJob.encodePassword()
        userJob.generateKeys()
        BasicInstanceBuilder.saveDomain(userJob)

        println "username="+userJob.username
        println "password="+userJob.password
        println "enabled="+userJob.enabled

        println "db="+UserJob.read(json.userJob.id)
        User.findByUsername(Infos.GOODLOGIN).getAuthorities().each { secRole ->
            SecUserSecRole.create(userJob, secRole)
        }

//        UserJob userJob2 = new UserJob(username: "BasicUserJob",password: "PasswordUserJob",enabled: true,user : User.findByUsername(Infos.GOODLOGIN),job: BasicInstanceBuilder.getJob())
//        userJob2.generateKeys()
//        BasicInstanceBuilder.saveDomain(userJob2)
//        User.findByUsername(Infos.GOODLOGIN).getAuthorities().each { secRole ->
//            SecUserSecRole.create(userJob2, secRole)
//        }
//
//        println "************************** userJob"
//        userJob.properties.each {
//            println it.key+"="+it.value
//        }
//
//        println "************************** userJob2"
//        userJob2.properties.each {
//            println it.key+"="+it.value
//        }

        //get job from user (with userjob cred)
        assert (200 == JobAPI.show(job.id,userJob.username,"password").code)
//        assert (200 == JobAPI.show(-1,userJob2.username,"PasswordUserJob").code)

        // get image from user (with userjob cred)
        assert (200 == ImageInstanceAPI.listByProject(project.id,userJob.username,"password").code)
    }
}
