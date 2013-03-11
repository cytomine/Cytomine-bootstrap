package be.cytomine

import be.cytomine.processing.SoftwareParameter
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.SoftwareParameterAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SoftwareParameterTests  {

    void testListSoftwareParameterWithCredential() {
          def result = SoftwareParameterAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
          def json = JSON.parse(result.data)
          assert json.collection instanceof JSONArray
      }
  
      void testListSoftwareParameterBySoftware() {
          SoftwareParameter softwareparameter = BasicInstanceBuilder.getSoftwareParameter()
          def result = SoftwareParameterAPI.listBySoftware(softwareparameter.software.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
          def json = JSON.parse(result.data)
          assert json.collection instanceof JSONArray
          result = SoftwareParameterAPI.listBySoftware(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 404 == result.code
      }
  
      void testShowSoftwareParameterWithCredential() {
          def result = SoftwareParameterAPI.show(BasicInstanceBuilder.getSoftwareParameter().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
          def json = JSON.parse(result.data)
          assert json instanceof JSONObject
      }
  
      void testAddSoftwareParameterCorrect() {
          def softwareparameterToAdd = BasicInstanceBuilder.getSoftwareParameterNotExist()
          def result = SoftwareParameterAPI.create(softwareparameterToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
          int idSoftwareParameter = result.data.id
    
          result = SoftwareParameterAPI.show(idSoftwareParameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
      }

      void testUpdateSoftwareParameterCorrect() {
          def sp = BasicInstanceBuilder.getSoftwareParameter()
          def data = UpdateData.createUpdateSet(sp,[name: ["OLDVALUE","NEWVALUE"]])
          def result = SoftwareParameterAPI.update(sp.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
          def json = JSON.parse(result.data)
          assert json instanceof JSONObject
      }
  
      void testUpdateSoftwareParameterNotExist() {
          SoftwareParameter softwareparameterWithNewName = BasicInstanceBuilder.getSoftwareParameterNotExist()
          softwareparameterWithNewName.save(flush: true)
          SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterWithNewName.id)
          def jsonSoftwareParameter = softwareparameterToEdit.encodeAsJSON()
          def jsonUpdate = JSON.parse(jsonSoftwareParameter)
          jsonUpdate.id = -99
          jsonSoftwareParameter = jsonUpdate.encodeAsJSON()
          def result = SoftwareParameterAPI.update(-99, jsonSoftwareParameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 404 == result.code
      }
  
      void testUpdateSoftwareParameterWithBadSoftware() {
          SoftwareParameter softwareparameterToAdd = BasicInstanceBuilder.getSoftwareParameter()
          SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterToAdd.id)
          def jsonSoftwareParameter = softwareparameterToEdit.encodeAsJSON()
          def jsonUpdate = JSON.parse(jsonSoftwareParameter)
          jsonUpdate.software = -99
          jsonSoftwareParameter = jsonUpdate.encodeAsJSON()
          def result = SoftwareParameterAPI.update(softwareparameterToAdd.id, jsonSoftwareParameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 400 == result.code
      }
  
      void testDeleteSoftwareParameter() {
          def softwareparameterToDelete = BasicInstanceBuilder.getSoftwareParameterNotExist()
          assert softwareparameterToDelete.save(flush: true)!= null
          def id = softwareparameterToDelete.id
          def result = SoftwareParameterAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
  
          def showResult = SoftwareParameterAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 404 == showResult.code
      }
  
      void testDeleteSoftwareParameterNotExist() {
          def result = SoftwareParameterAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 404 == result.code
      }
}
