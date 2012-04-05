package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.JobParameter
import be.cytomine.test.http.JobParameterAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class JobParameterTests extends functionaltestplugin.FunctionalTestCase {

    void testListJobParameterWithCredential() {
         def result = JobParameterAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
         def json = JSON.parse(result.data)
         assert json instanceof JSONArray
     }
 
     void testListJobParameterByJob() {
         JobParameter jobparameter = BasicInstance.createOrGetBasicJobParameter()
         def result = JobParameterAPI.listByJob(jobparameter.job.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
         def json = JSON.parse(result.data)
         assert json instanceof JSONArray
     }
 
     void testShowJobParameterWithCredential() {
         def result = JobParameterAPI.show(BasicInstance.createOrGetBasicJobParameter().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
         def json = JSON.parse(result.data)
         assert json instanceof JSONObject
     }
 
     void testAddJobParameterCorrect() {
         def jobparameterToAdd = BasicInstance.getBasicJobParameterNotExist()
         def result = JobParameterAPI.create(jobparameterToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
         int idJobParameter = result.data.id
   
         result = JobParameterAPI.show(idJobParameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
     }
 
     void testAddJobParameterWithBadJob() {
         JobParameter jobparameterToAdd = BasicInstance.createOrGetBasicJobParameter()
         JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
         def jsonJobParameter = jobparameterToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonJobParameter)
         jsonUpdate.job = -99
         jsonJobParameter = jsonUpdate.encodeAsJSON()
         def result = JobParameterAPI.update(jobparameterToAdd.id, jsonJobParameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(400, result.code)
     }
 
     void testUpdateJobParameterCorrect() {
         JobParameter jobparameterToAdd = BasicInstance.createOrGetBasicJobParameter()
         def result = JobParameterAPI.update(jobparameterToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
         def json = JSON.parse(result.data)
         assert json instanceof JSONObject
     }
 
     void testUpdateJobParameterNotExist() {
         JobParameter jobparameterWithNewName = BasicInstance.getBasicJobParameterNotExist()
         jobparameterWithNewName.save(flush: true)
         JobParameter jobparameterToEdit = JobParameter.get(jobparameterWithNewName.id)
         def jsonJobParameter = jobparameterToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonJobParameter)
         jsonUpdate.id = -99
         jsonJobParameter = jsonUpdate.encodeAsJSON()
         def result = JobParameterAPI.update(-99, jsonJobParameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(404, result.code)
     }
 
     void testUpdateJobParameterWithBadJob() {
         JobParameter jobparameterToAdd = BasicInstance.createOrGetBasicJobParameter()
         JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
         def jsonJobParameter = jobparameterToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonJobParameter)
         jsonUpdate.job = -99
         jsonJobParameter = jsonUpdate.encodeAsJSON()
         def result = JobParameterAPI.update(jobparameterToAdd.id, jsonJobParameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(400, result.code)
     }
 
     void testDeleteJobParameter() {
         def jobparameterToDelete = BasicInstance.getBasicJobParameterNotExist()
         assert jobparameterToDelete.save(flush: true)!= null
         def id = jobparameterToDelete.id
         def result = JobParameterAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
 
         def showResult = JobParameterAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(404, showResult.code)
     }
 
     void testDeleteJobParameterNotExist() {
         def result = JobParameterAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(404, result.code)
     }
}
