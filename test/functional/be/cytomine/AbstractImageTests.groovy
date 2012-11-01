package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.AbstractImageAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 13:49
 * To change this template use File | Settings | File Templates.
 */
class AbstractImageTests extends functionaltestplugin.FunctionalTestCase{

  void testListImagesWithCredential() {
      BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListImagesWithoutCredential() {
      BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
      assertEquals(401, result.code)
  }

  void testListAnnotationsByUserWithCredential() {
      BasicInstance.createOrGetBasicAbstractImage()
      User user = BasicInstance.createOrGetBasicUser()
      def result = AbstractImageAPI.listByUser(user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListAnnotationsByUserNoExistWithCredential() {
      def result = AbstractImageAPI.listByUser(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testGetImageWithCredential() {
      AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.show(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddImageCorrect() {
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      String json = imageToAdd.encodeAsJSON()
      def result = AbstractImageAPI.create(json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      int id = result.data.id
      result = AbstractImageAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
  }

  void testaddImageWithUnexistingScanner() {
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.scanner = -99
      def result = AbstractImageAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

  void testaddImageWithUnexistingSlide() {
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.sample = -99
      def result = AbstractImageAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

  void testaddImageWithUnexistingMime() {
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.mime = -99
      def result = AbstractImageAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

  void testaddImageWithUnexistingImageServer() {
    def mime = BasicInstance.getBasicMimeNotExist()
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.mime = mime.id
      def result = AbstractImageAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

  void testEditImage() {
      AbstractImage imageToAdd = BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.update(imageToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int id = json.abstractimage.id
      def showResult = AbstractImageAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstance.compareAbstractImage(result.mapNew, json)
  }

  void testDeleteImage()  {
      def imageToDelete = BasicInstance.createOrGetBasicAbstractImage()
      Long id = imageToDelete.id
      def result = AbstractImageAPI.delete(id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200,result.code)
      result = AbstractImageAPI.show(id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(404,result.code)
  }

  void testDeleteImageWithData()  {
    def imageToDelete = BasicInstance.createOrGetBasicImageInstance()
    def annotation = BasicInstance.createOrGetBasicUserAnnotation()
    annotation.image = imageToDelete
    annotation.save(flush:true)
      Long id = imageToDelete.baseImage.id
      def result = AbstractImageAPI.delete(id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(400,result.code)
  }

  void testDeleteImageNoExist()  {
      def result = AbstractImageAPI.delete(-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(404,result.code)
  }

}