package be.cytomine

import be.cytomine.image.UploadedFile
import be.cytomine.project.Discipline
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.DisciplineAPI
import be.cytomine.test.http.UploadedFileAPI
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
class UploadedFileTests {

  void testListUploadedFil() {
      def result = UploadedFileAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testShowUploadedFileWithCredential() {
      def result = UploadedFileAPI.show(BasicInstanceBuilder.getUploadedFile().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddUploadedFileCorrect() {
      def uploadedfileToAdd = BasicInstanceBuilder.getUploadedFileNotExist()
      def result = UploadedFileAPI.create(uploadedfileToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      int idUploadedFile = result.data.id

      result = UploadedFileAPI.show(idUploadedFile, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
  }


  void testUpdateUploadedFileCorrect() {
      def uploadedfile = BasicInstanceBuilder.getUploadedFile()
      def data = UpdateData.createUpdateSet(uploadedfile,[status: [0,4]])
      def result = UploadedFileAPI.update(uploadedfile.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idUploadedFile = json.uploadedfile.id

      def showResult = UploadedFileAPI.show(idUploadedFile, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstanceBuilder.compare(data.mapNew, json)
  }

  void testDeleteUploadedFile() {
      def uploadedfileToDelete = BasicInstanceBuilder.getUploadedFileNotExist()
      assert uploadedfileToDelete.save(flush: true)!= null
      def id = uploadedfileToDelete.id
      def result = UploadedFileAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      def showResult = UploadedFileAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == showResult.code

  }

  void testDeleteUploadedFileNotExist() {
      def result = UploadedFileAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }
}
