package be.cytomine

import be.cytomine.ontology.AlgoAnnotation

import be.cytomine.project.Project

import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import be.cytomine.ontology.AlgoAnnotationTerm

import be.cytomine.test.http.JobAPI
import be.cytomine.command.Task
import be.cytomine.test.http.TaskAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 17/02/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class TaskTests  {

    void testShowTask() {
        println "test task"
        Task task = new Task(project: BasicInstance.createOrGetBasicProject(),user:  BasicInstance.createOrGetBasicUser())
        task.progress = 50
        task.addToComments("First step...")
        task.addToComments("Second step...")
        BasicInstance.checkDomain(task)
        BasicInstance.saveDomain(task)

        println "task.progress="+task.progress
        println "task.comments="+task.comments


        def result = TaskAPI.show(task.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        println "task.json="+json
        assert json.progress == 50
        assert json.comments.size()==2
        assert json.comments[0].equals("First step...")
        assert json.comments[1].equals("Second step...")

        assert json.project == task.project.id
        assert json.user == task.user.id
    }

    void testShowTaskNotExist() {
        def result = TaskAPI.show(-99, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testTask() {
        Task task = new Task(project: BasicInstance.createOrGetBasicProject(),user:  BasicInstance.createOrGetBasicUser())
        assert task.progress==0
        assert task.comments==null
        task.comments = new ArrayList<String>()
        task.addToComments("First step...")
        task.addToComments("Second step...")
        task.save(flush: true)
        assert task.comments.size()==2
    }

    void testAddTask() {
        Project project = BasicInstance.createOrGetBasicProject()
        def result = TaskAPI.create(project.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)

        assert json instanceof JSONObject
        println "task.json="+json
        assert json.task.progress == 0
        //assert json.comments.size()==1
        assert json.task.project == project.id
    }

    void testAddTaskProjectNotFound() {
        def result = TaskAPI.create(-99, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testConcreteTask() {
        //create a job
        Job job = BasicInstance.getBasicJobNotExist()
        BasicInstance.checkDomain(job)
        BasicInstance.saveDomain(job)
        BasicInstance.createSoftwareProject(job.software,job.project)

        UserJob userJob = BasicInstance.createBasicUserJobNotExist()
        userJob.job = job
        userJob.user = BasicInstance.getNewUser()
        BasicInstance.checkDomain(userJob)
        BasicInstance.saveDomain(userJob)

        //add algo-annotation for this job
        AlgoAnnotation a1 = BasicInstance.createAlgoAnnotation(job,userJob)

        //add algo-annotation-term for this job
        AlgoAnnotationTerm at1 = BasicInstance.createAlgoAnnotationTerm(job,a1,userJob)

        def result = TaskAPI.create(job.project.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def jsonTask = JSON.parse(result.data)


        //delete all job data
        result = JobAPI.deleteAllJobData(job.id, jsonTask.task.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def task = Task.read(jsonTask.task.id)
        task.refresh()
        assert task.progress==100
    }

}
