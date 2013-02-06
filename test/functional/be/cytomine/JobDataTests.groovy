package be.cytomine

import be.cytomine.processing.JobData
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.JobDataAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.utils.UpdateData

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
        def result = JobDataAPI.create(jobdataToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        JobData jobdata = result.data
        result = JobDataAPI.show(jobdata.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testEditJobDataCorrect() {
        JobData jobdataToAdd = BasicInstance.createOrGetBasicJobData()
        def data = UpdateData.createUpdateSet(jobdataToAdd)
        def result = JobDataAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idJobData = json.jobdata.id
        def showResult = JobDataAPI.show(idJobData, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        println "data.mapNew="+data.mapNew
        println "json="+json
        BasicInstance.compareJobData(data.mapNew, json)
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

    //testUpload + download from database
    void testDataInDatabase() {
        ConfigurationHolder.config.cytomine.jobdata.filesystem = false
        //create jobdata
        JobData jobData = BasicInstance.getBasicJobDataNotExist()
        jobData.filename = "test.data"
        jobData.save(flush: true)

        byte[] testData = "HelloWorld!".bytes

        //upload file
         def result = JobDataAPI.upload(jobData.id,testData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        //download file
        result = JobDataAPI.download(jobData.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        //check if byte[] are equals
        assertEquals testData, result.data
    }

    //testUpload + download from filesystem
    void testDataInFileSystem() {
        ConfigurationHolder.config.cytomine.jobdata.filesystem = true
        //create jobdata
        JobData jobData = BasicInstance.getBasicJobDataNotExist()
        jobData.filename = "test.data"
        jobData.save(flush: true)

        byte[] testData = "HelloWorld!".bytes

        //upload file
         def result = JobDataAPI.upload(jobData.id,testData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        //download file
        result = JobDataAPI.download(jobData.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        //check if byte[] are equals
        assertEquals testData, result.data
        ConfigurationHolder.config.cytomine.jobdata.filesystem = false
    }

    protected void tearDown() {
        super.tearDown();
        ConfigurationHolder.config.cytomine.jobdata.filesystem = false
    }

    protected void setUp() {
        super.tearDown();
        ConfigurationHolder.config.cytomine.jobdata.filesystem = true
    }


}
