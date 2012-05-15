package be.cytomine

import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.JobData
import be.cytomine.test.http.JobDataAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 17/02/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class JobDataTests extends functionaltestplugin.FunctionalTestCase {

    void testListJobDataWithCredential() {
        def result = JobDataAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListJobDataWithoutCredential() {
        def result = JobDataAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assertEquals(401, result.code)
    }

    void testShowJobDataWithCredential() {
        JobData jobdata = BasicInstance.createOrGetBasicJobData()
        def result = JobDataAPI.show(jobdata.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListJobDataByJob() {
        JobData jobdata = BasicInstance.createOrGetBasicJobData()
        def result = JobDataAPI.listByJob(jobdata.job.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListJobDataByJobNotExist() {
        def result = JobDataAPI.listByJob(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }


    void testAddJobDataCorrect() {
        def jobdataToAdd = BasicInstance.getBasicJobDataNotExist()
        def result = JobDataAPI.create(jobdataToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        JobData jobdata = result.data
        result = JobDataAPI.show(jobdata.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testEditJobDataCorrect() {
        JobData jobdataToAdd = BasicInstance.createOrGetBasicJobData()
        def result = JobDataAPI.update(jobdataToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idJobData = json.jobdata.id
        def showResult = JobDataAPI.show(idJobData, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareJobData(result.mapNew, json)
    }

    void testEditJobDataWithBadKey() {
        JobData jobdataToAdd = BasicInstance.createOrGetBasicJobData()
        JobData jobdataToEdit = JobData.get(jobdataToAdd.id)
        def jsonJobData = jobdataToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobData)
        jsonUpdate.key = null
        jsonJobData = jsonUpdate.encodeAsJSON()
        def result = JobDataAPI.update(jobdataToAdd.id, jsonJobData, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testDeleteJobData() {
        def jobdataToDelete = BasicInstance.getBasicJobDataNotExist()
        assert jobdataToDelete.save(flush: true) != null
        def result = JobDataAPI.delete(jobdataToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def showResult = JobDataAPI.show(jobdataToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)
    }

    void testDeleteJobDataNotExist() {
        def result = JobDataAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }
}
