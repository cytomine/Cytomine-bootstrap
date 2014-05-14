package be.cytomine

import be.cytomine.laboratory.Sample
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.SampleAPI
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
class SampleTests  {

  void testListSampleWithCredential() {
      def result = SampleAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testListSampleWithoutCredential() {
      def result = SampleAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
      assert 401 == result.code
  }

  void testShowSampleWithCredential() {
      def result = SampleAPI.show(BasicInstanceBuilder.getSample().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddSampleCorrect() {
      def sampleToAdd = BasicInstanceBuilder.getSampleNotExist()
      def result = SampleAPI.create(sampleToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      int idSample = result.data.id

      result = SampleAPI.show(idSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = SampleAPI.undo()
      assert 200 == result.code

      result = SampleAPI.show(idSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code

      result = SampleAPI.redo()
      assert 200 == result.code

      result = SampleAPI.show(idSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
  }

  void testAddSampleAlreadyExist() {
      def sampleToAdd = BasicInstanceBuilder.getSample()
      def result = SampleAPI.create(sampleToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 409 == result.code
  }

  void testUpdateSampleCorrect() {
      Sample sampleToAdd = BasicInstanceBuilder.getSample()
      def data = UpdateData.createUpdateSet(sampleToAdd,[name: ["OLDNAME","NEWNAME"]])
      def result = SampleAPI.update(sampleToAdd.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idSample = json.sample.id

      def showResult = SampleAPI.show(idSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstanceBuilder.compare(data.mapNew, json)

      showResult = SampleAPI.undo()
      assert 200 == result.code
      showResult = SampleAPI.show(idSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

      showResult = SampleAPI.redo()
      assert 200 == result.code
      showResult = SampleAPI.show(idSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
  }

  void testUpdateSampleNotExist() {
      Sample sampleWithOldName = BasicInstanceBuilder.getSample()
      Sample sampleWithNewName = BasicInstanceBuilder.getSampleNotExist()
      sampleWithNewName.save(flush: true)
      Sample sampleToEdit = Sample.get(sampleWithNewName.id)
      def jsonSample = sampleToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonSample)
      jsonUpdate.name = sampleWithOldName.name
      jsonUpdate.id = -99
      jsonSample = jsonUpdate.toString()
      def result = SampleAPI.update(-99, jsonSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  void testUpdateSampleWithNameAlreadyExist() {
      Sample sampleWithOldName = BasicInstanceBuilder.getSample()
      Sample sampleWithNewName = BasicInstanceBuilder.getSampleNotExist()
      sampleWithNewName.save(flush: true)
      Sample sampleToEdit = Sample.get(sampleWithNewName.id)
      def jsonSample = sampleToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonSample)
      jsonUpdate.name = sampleWithOldName.name
      jsonSample = jsonUpdate.toString()
      def result = SampleAPI.update(sampleToEdit.id, jsonSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 409 == result.code
  }
    
    void testEditSampleWithBadName() {
        Sample sampleToAdd = BasicInstanceBuilder.getSample()
        Sample sampleToEdit = Sample.get(sampleToAdd.id)
        def jsonSample = sampleToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSample)
        jsonUpdate.name = null
        jsonSample = jsonUpdate.toString()
        def result = SampleAPI.update(sampleToAdd.id, jsonSample, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

  void testDeleteSample() {
      def sampleToDelete = BasicInstanceBuilder.getSampleNotExist()
      assert sampleToDelete.save(flush: true)!= null
      def id = sampleToDelete.id
      def result = SampleAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      def showResult = SampleAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == showResult.code

      result = SampleAPI.undo()
      assert 200 == result.code

      result = SampleAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = SampleAPI.redo()
      assert 200 == result.code

      result = SampleAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  void testDeleteSampleNotExist() {
      def result = SampleAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }
}
