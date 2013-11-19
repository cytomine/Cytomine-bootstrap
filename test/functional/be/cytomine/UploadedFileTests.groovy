package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.Storage
import be.cytomine.project.Discipline
import be.cytomine.security.User
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

   void testUploadFileWorkflow() {
       def oneAnotherImage = BasicInstanceBuilder.initImage()
       UploadedFile uploadedFile = new UploadedFile()
       uploadedFile.originalFilename = "test.tif"
       uploadedFile.filename = "/data/test.cytomine.be/1/1383567901007/test.tif"
       uploadedFile.path = "/tmp/imageserver_buffer"
       uploadedFile.size = 243464757l
       uploadedFile.ext = "tif"
       uploadedFile.contentType  = "image/tiff"
       uploadedFile.storages = new Long[1]
       uploadedFile.storages[0] = Storage.findByName("lrollus test storage").id
       uploadedFile.projects = new Long[1]
       uploadedFile.projects[0] = oneAnotherImage.project.id
       uploadedFile.user = oneAnotherImage.user
       uploadedFile.status = UploadedFile.TO_DEPLOY
       BasicInstanceBuilder.saveDomain(uploadedFile)

       def result = UploadedFileAPI.createImage(uploadedFile.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
       def image = AbstractImage.findByFilename("/data/test.cytomine.be/1/1383567901007/test.tif")
       assert image


       result = UploadedFileAPI.clearAbstractImageProperties(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
       result = UploadedFileAPI.populateAbstractImageProperties(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
       result = UploadedFileAPI.extractUsefulAbstractImageProperties(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code

   }


    void testUploadFileWorkflowForGhest() {
        User ghest = BasicInstanceBuilder.getGhest("GHESTUPLOAD","PASSWORD")
        def oneAnotherImage = BasicInstanceBuilder.initImage()
        UploadedFile uploadedFile = new UploadedFile()
        uploadedFile.originalFilename = "test.tif"
        uploadedFile.filename = "/data/test.cytomine.be/1/1383567901007/test.tif"
        uploadedFile.path = "/tmp/imageserver_buffer"
        uploadedFile.size = 243464757l
        uploadedFile.ext = "tif"
        uploadedFile.contentType  = "image/tiff"
        uploadedFile.storages = new Long[1]
        uploadedFile.storages[0] = Storage.findByName("lrollus test storage").id
        uploadedFile.projects = new Long[1]
        uploadedFile.projects[0] = oneAnotherImage.project.id
        uploadedFile.user = oneAnotherImage.user
        uploadedFile.status = UploadedFile.TO_DEPLOY
        BasicInstanceBuilder.saveDomain(uploadedFile)

        def result = UploadedFileAPI.createImage(uploadedFile.id,"GHESTUPLOAD", "PASSWORD")
        assert 403 == result.code

    }




}
