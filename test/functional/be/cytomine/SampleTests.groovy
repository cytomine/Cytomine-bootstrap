package be.cytomine

import be.cytomine.test.Infos
import be.cytomine.utils.BasicInstance
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.test.http.SampleAPI
import be.cytomine.laboratory.Sample

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SampleTests extends functionaltestplugin.FunctionalTestCase {

  void testListSampleWithCredential() {
      def result = SampleAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListSampleWithoutCredential() {
      def result = SampleAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
      assertEquals(401, result.code)
  }

  void testShowSampleWithCredential() {
      def result = SampleAPI.show(BasicInstance.createOrGetBasicSample().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddSampleCorrect() {
      def sampleToAdd = BasicInstance.getBasicSampleNotExist()
      def result = SampleAPI.create(sampleToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      int idSample = result.data.id

      result = SampleAPI.show(idSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)

      result = SampleAPI.undo()
      assertEquals(200, result.code)

      result = SampleAPI.show(idSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)

      result = SampleAPI.redo()
      assertEquals(200, result.code)

      result = SampleAPI.show(idSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
  }

  void testAddSampleAlreadyExist() {
      def sampleToAdd = BasicInstance.createOrGetBasicSample()
      def result = SampleAPI.create(sampleToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(409, result.code)
  }

  void testUpdateSampleCorrect() {
      Sample sampleToAdd = BasicInstance.createOrGetBasicSample()

      def data = UpdateData.createUpdateSet(sampleToAdd)
      def result = SampleAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idSample = json.sample.id

      def showResult = SampleAPI.show(idSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstance.compareSample(data.mapNew, json)

      showResult = SampleAPI.undo()
      assertEquals(200, result.code)
      showResult = SampleAPI.show(idSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      BasicInstance.compareSample(data.mapOld, JSON.parse(showResult.data))

      showResult = SampleAPI.redo()
      assertEquals(200, result.code)
      showResult = SampleAPI.show(idSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      BasicInstance.compareSample(data.mapNew, JSON.parse(showResult.data))
  }

  void testUpdateSampleNotExist() {
      Sample sampleWithOldName = BasicInstance.createOrGetBasicSample()
      Sample sampleWithNewName = BasicInstance.getBasicSampleNotExist()
      sampleWithNewName.save(flush: true)
      Sample sampleToEdit = Sample.get(sampleWithNewName.id)
      def jsonSample = sampleToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonSample)
      jsonUpdate.name = sampleWithOldName.name
      jsonUpdate.id = -99
      jsonSample = jsonUpdate.encodeAsJSON()
      def result = SampleAPI.update(-99, jsonSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testUpdateSampleWithNameAlreadyExist() {
      Sample sampleWithOldName = BasicInstance.createOrGetBasicSample()
      Sample sampleWithNewName = BasicInstance.getBasicSampleNotExist()
      sampleWithNewName.save(flush: true)
      Sample sampleToEdit = Sample.get(sampleWithNewName.id)
      def jsonSample = sampleToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonSample)
      jsonUpdate.name = sampleWithOldName.name
      jsonSample = jsonUpdate.encodeAsJSON()
      def result = SampleAPI.update(sampleToEdit.id, jsonSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(409, result.code)
  }
    
    void testEditSampleWithBadName() {
        Sample sampleToAdd = BasicInstance.createOrGetBasicSample()
        Sample sampleToEdit = Sample.get(sampleToAdd.id)
        def jsonSample = sampleToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSample)
        jsonUpdate.name = null
        jsonSample = jsonUpdate.encodeAsJSON()
        def result = SampleAPI.update(sampleToAdd.id, jsonSample, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

  void testDeleteSample() {
      def sampleToDelete = BasicInstance.getBasicSampleNotExist()
      assert sampleToDelete.save(flush: true)!= null
      def id = sampleToDelete.id
      def result = SampleAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)

      def showResult = SampleAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, showResult.code)

      result = SampleAPI.undo()
      assertEquals(200, result.code)

      result = SampleAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)

      result = SampleAPI.redo()
      assertEquals(200, result.code)

      result = SampleAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testDeleteSampleNotExist() {
      def result = SampleAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }
}
