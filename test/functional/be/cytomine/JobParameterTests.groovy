package be.cytomine

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.processing.JobParameter
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.JobParameterAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class JobParameterTests  {

    void testListJobParameterWithCredential() {
         def result = JobParameterAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
     }
 
     void testListJobParameterByJob() {
         JobParameter jobparameter = BasicInstanceBuilder.getJobParameter()
         def result = JobParameterAPI.listByJob(jobparameter.job.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray

        result = JobParameterAPI.listByJob(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == result.code
     }
 
     void testShowJobParameterWithCredential() {
         def result = JobParameterAPI.show(BasicInstanceBuilder.getJobParameter().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json instanceof JSONObject
     }
 
     void testAddJobParameterCorrect() {
         def jobparameterToAdd = BasicInstanceBuilder.getJobParameterNotExist()
         def result = JobParameterAPI.create(jobparameterToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         int idJobParameter = result.data.id
   
         result = JobParameterAPI.show(idJobParameter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
     }
 
     void testAddJobParameterWithBadJob() {
         JobParameter jobparameterToAdd = BasicInstanceBuilder.getJobParameter()
         JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
         def jsonJobParameter = jobparameterToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonJobParameter)
         jsonUpdate.job = -99
         jsonJobParameter = jsonUpdate.toString()
         def result = JobParameterAPI.update(jobparameterToAdd.id, jsonJobParameter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 400 == result.code
     }

    void testAddJobParameterAlreadyExist() {
        def jobparameterToAdd = BasicInstanceBuilder.getJobParameterNotExist()
        def result = JobParameterAPI.create(jobparameterToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        result = JobParameterAPI.create(jobparameterToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }
 
     void testUpdateJobParameterCorrect() {
         def jobParam = BasicInstanceBuilder.getJobParameter()
         def data = UpdateData.createUpdateSet(jobParam,[value: ["123","456"]])
         def result = JobParameterAPI.update(jobParam.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json instanceof JSONObject
     }
 
     void testUpdateJobParameterNotExist() {
         JobParameter jobparameterWithNewName = BasicInstanceBuilder.getJobParameterNotExist()
         jobparameterWithNewName.save(flush: true)
         JobParameter jobparameterToEdit = JobParameter.get(jobparameterWithNewName.id)
         def jsonJobParameter = jobparameterToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonJobParameter)
         jsonUpdate.id = -99
         jsonJobParameter = jsonUpdate.toString()
         def result = JobParameterAPI.update(-99, jsonJobParameter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == result.code
     }
 
     void testUpdateJobParameterWithBadJob() {
         JobParameter jobparameterToAdd = BasicInstanceBuilder.getJobParameter()
         JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
         def jsonJobParameter = jobparameterToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonJobParameter)
         jsonUpdate.job = -99
         jsonJobParameter = jsonUpdate.toString()
         def result = JobParameterAPI.update(jobparameterToAdd.id, jsonJobParameter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 400 == result.code
     }
 
     void testDeleteJobParameter() {
         def jobparameterToDelete = BasicInstanceBuilder.getJobParameterNotExist()
         assert jobparameterToDelete.save(flush: true)!= null
         def id = jobparameterToDelete.id
         def result = JobParameterAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
 
         def showResult = JobParameterAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == showResult.code
     }
 
     void testDeleteJobParameterNotExist() {
         def result = JobParameterAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == result.code
     }
}
