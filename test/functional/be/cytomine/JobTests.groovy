package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.Job
import be.cytomine.test.http.JobAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class JobTests extends functionaltestplugin.FunctionalTestCase {

    void testListJobWithCredential() {
        def result = JobAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListJobBySoftwareWithCredential() {
        Job job = BasicInstance.createOrGetBasicJob()
        def result = JobAPI.listBySoftware(job.software.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListJobBySoftwareAndProjectWithCredential() {
        Job job = BasicInstance.createOrGetBasicJob()
        def result = JobAPI.listBySoftwareAndProject(job.software.id,job.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,false)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListJobBySoftwareAndProjectWithCredentialLight() {
        Job job = BasicInstance.createOrGetBasicJob()
        def result = JobAPI.listBySoftwareAndProject(job.software.id,job.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,true)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testShowJobWithCredential() {
        def result = JobAPI.show(BasicInstance.createOrGetBasicJob().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddJobCorrect() {
        def jobToAdd = BasicInstance.getBasicJobNotExist()
        def result = JobAPI.create(jobToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idJob = result.data.id
  
        result = JobAPI.show(idJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddJobWithBadSoftware() {
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.software = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(jobToAdd.id, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testUpdateJobCorrect() {
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        def result = JobAPI.update(jobToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testUpdateJobNotExist() {
        Job jobWithNewName = BasicInstance.getBasicJobNotExist()
        jobWithNewName.save(flush: true)
        Job jobToEdit = Job.get(jobWithNewName.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.id = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(-99, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testUpdateJobWithBadSoftware() {
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.software = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(jobToAdd.id, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testDeleteJob() {
        def jobToDelete = BasicInstance.getBasicJobNotExist()
        assert jobToDelete.save(flush: true)!= null
        def id = jobToDelete.id
        def result = JobAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        def showResult = JobAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)
    }

    void testDeleteJobNotExist() {
        def result = JobAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }
}
