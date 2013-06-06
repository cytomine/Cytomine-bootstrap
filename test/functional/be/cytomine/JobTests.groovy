package be.cytomine

import be.cytomine.processing.Job
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.JobAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import be.cytomine.security.UserJob
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.processing.JobData

import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class JobTests  {

    void testListJobWithCredential() {
        def result = JobAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListJobBySoftwareAndProjectWithCredential() {
        Job job = BasicInstanceBuilder.getJob()
        def result = JobAPI.listBySoftwareAndProject(job.software.id,job.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,false)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = JobAPI.listBySoftwareAndProject(-99,job.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,false)
        assert 404 == result.code

        result = JobAPI.listBySoftwareAndProject(job.software.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD,false)
        assert 404 == result.code
    }

    void testListJobBySoftwareAndProjectWithCredentialLight() {
        Job job = BasicInstanceBuilder.getJob()
        def result = JobAPI.listBySoftwareAndProject(job.software.id,job.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,true)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testShowJobWithCredential() {
        def result = JobAPI.show(BasicInstanceBuilder.getJob().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddJobCorrect() {
        def jobToAdd = BasicInstanceBuilder.getJobNotExist()
        def result = JobAPI.create(jobToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idJob = result.data.id
  
        result = JobAPI.show(idJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddJobWithBadSoftware() {
        Job jobToAdd = BasicInstanceBuilder.getJob()
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.software = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(jobToAdd.id, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testUpdateJobCorrect() {
        def job =  BasicInstanceBuilder.getJob()
        def data = UpdateData.createUpdateSet(job,[progress: [0,100]])
        def result = JobAPI.update(job.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testUpdateJobNotExist() {
        Job jobWithNewName = BasicInstanceBuilder.getJobNotExist()
        jobWithNewName.save(flush: true)
        Job jobToEdit = Job.get(jobWithNewName.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.id = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(-99, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testUpdateJobWithBadSoftware() {
        Job jobToAdd = BasicInstanceBuilder.getJob()
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.software = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(jobToAdd.id, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testDeleteJob() {
        def jobToDelete = BasicInstanceBuilder.getJobNotExist()
        assert jobToDelete.save(flush: true)!= null
        def id = jobToDelete.id
        def result = JobAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def showResult = JobAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code
    }

    void testDeleteJobNotExist() {
        def result = JobAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testListJobData() {
        Job job = BasicInstanceBuilder.getJob()
        def result = JobAPI.listAllJobData(job.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = JobAPI.listAllJobData(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testDeleteAllJobDataJobNotExist() {
        def result = JobAPI.deleteAllJobData(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteAllJobData() {
        //create a job
        Job job = BasicInstanceBuilder.getJobNotExist(true)
        BasicInstanceBuilder.getSoftwareProjectNotExist(job.software,job.project,true)

        UserJob userJob = BasicInstanceBuilder.getUserJobNotExist(true)
        userJob.job = job
        userJob.user = BasicInstanceBuilder.getUser()
        BasicInstanceBuilder.saveDomain(userJob)

        //add algo-annotation for this job
        AlgoAnnotation a1 = BasicInstanceBuilder.getAlgoAnnotationNotExist(job,userJob,true)

        //add algo-annotation-term for this job
        AlgoAnnotationTerm at1 = BasicInstanceBuilder.getAlgoAnnotationTerm(job,a1,userJob)

        //add job data
        JobData data1 = BasicInstanceBuilder.getJobDataNotExist()
        data1.job = job
        BasicInstanceBuilder.checkDomain(data1)
        BasicInstanceBuilder.saveDomain(data1)


        //count data = 1-1
        assert AlgoAnnotationTerm.findAllByUserJobInList(UserJob.findAllByJob(job)).size() == 1
        assert AlgoAnnotation.findAllByUserInList(UserJob.findAllByJob(job)).size() == 1
        assert JobData.findAllByJob(job).size() == 1

        //delete all job data
        def result = JobAPI.deleteAllJobData(job.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        //count data = 0-0
        assert AlgoAnnotationTerm.findAllByUserJobInList(UserJob.findAllByJob(job)).size() == 0
        assert AlgoAnnotation.findAllByUserInList(UserJob.findAllByJob(job)).size() == 0
        assert JobData.findAllByJob(job).size() == 0
    }

    void testDeleteAllJobDataWithReviewedAnnotations() {
        //create a job

        UserJob userJob = BasicInstanceBuilder.getUserJobNotExist(true)
        Job job = userJob.job

        //add algo-annotation for this job
        AlgoAnnotation a1 = BasicInstanceBuilder.getAlgoAnnotationNotExist(job,userJob,true,)

        //add algo-annotation-term for this job
        AlgoAnnotationTerm at1 = BasicInstanceBuilder.getAlgoAnnotationTerm(job,a1,userJob)

        //add reviewed annotation
        ReviewedAnnotation reviewed = BasicInstanceBuilder.getReviewedAnnotationNotExist()
        reviewed.project = job.project
        reviewed.image = a1.image
        reviewed.parentIdent = a1.id
        reviewed.parentClassName = a1.class.getName()
        BasicInstanceBuilder.checkDomain(reviewed)
        BasicInstanceBuilder.saveDomain(reviewed)

        println "ReviewedAnnotation project=${reviewed.project.id} & parent=${reviewed.parentIdent}"
        println "ReviewedAnnotation job.project=${job.project.id}"
        //count data = 1-1
        assert AlgoAnnotationTerm.findAllByUserJobInList(UserJob.findAllByJob(job)).size() == 1
        assert AlgoAnnotation.findAllByUserInList(UserJob.findAllByJob(job)).size() == 1

        //delete all job data
        def result = JobAPI.deleteAllJobData(job.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }


}
