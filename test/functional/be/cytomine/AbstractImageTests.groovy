package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.image.AbstractImage
import be.cytomine.image.acquisition.Scanner
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.project.Slide
import be.cytomine.image.Mime

import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.test.http.AbstractImageAPI
import be.cytomine.test.http.AnnotationAPI

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
      def result = AbstractImageAPI.listAbstractImage(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListImagesWithoutCredential() {
      BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.listAbstractImage(Infos.BADLOGIN, Infos.BADPASSWORD)
      assertEquals(401, result.code)
  }

  void testListAnnotationsByUserWithCredential() {
      BasicInstance.createOrGetBasicAbstractImage()
      User user = BasicInstance.createOrGetBasicUser()
      def result = AbstractImageAPI.listAbstractImageByUser(user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListAnnotationsByUserNoExistWithCredential() {
      def result = AbstractImageAPI.listAbstractImageByUser(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testGetImageWithCredential() {
      AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.showAbstractImage(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddImageCorrect() {
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      String json = imageToAdd.encodeAsJSON()
      def result = AbstractImageAPI.createAbstractImage(json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      int id = result.data.id
      result = AbstractImageAPI.showAbstractImage(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
  }

  void testaddImageWithUnexistingScanner() {
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.scanner = -99
      def result = AbstractImageAPI.createAbstractImage(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

  void testaddImageWithUnexistingSlide() {
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.slide = -99
      def result = AbstractImageAPI.createAbstractImage(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

  void testaddImageWithUnexistingMime() {
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.mime = -99
      def result = AbstractImageAPI.createAbstractImage(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

  void testaddImageWithUnexistingImageServer() {
    def mime = BasicInstance.getBasicMimeNotExist()
      def imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.mime = mime.id
      def result = AbstractImageAPI.createAbstractImage(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

  void testEditImage() {
      AbstractImage imageToAdd = BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.updateAbstractImage(imageToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int id = json.abstractimage.id
      def showResult = AbstractImageAPI.showAbstractImage(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstance.compareAbstractImage(result.mapNew, json)
  }

  void testEditImageWithBadSlide() {
    Slide oldSlide = BasicInstance.createOrGetBasicSlide()
    Slide newSlide = BasicInstance.getBasicSlideNotExist()

    /* Create a old image */
    log.info("create image")
    AbstractImage imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
    imageToAdd.slide = oldSlide
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    AbstractImage imageToEdit = AbstractImage.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.slide = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testEditImageWithBadMime()  {
    Mime oldMime = BasicInstance.createOrGetBasicMime() //TODO: replace by a mime different with image server
    Mime newMime = BasicInstance.createOrGetBasicMime()  //jp2

    /* Create a old image */
    log.info("create image")
    AbstractImage imageToAdd = BasicInstance.createOrGetBasicAbstractImage()
    imageToAdd.mime = oldMime
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    AbstractImage imageToEdit = AbstractImage.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)
    jsonUpdate.mime = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testEditImageWithBadScanner()  {
    Scanner oldScanner = BasicInstance.createOrGetBasicScanner()

    /* Create a old image */
    log.info("create image")
    AbstractImage imageToAdd = BasicInstance.getBasicAbstractImageNotExist()
    imageToAdd.scanner = oldScanner
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    AbstractImage imageToEdit = AbstractImage.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.scanner = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testDeleteImage()  {

    log.info("create image")
    def imageToDelete = BasicInstance.getBasicAbstractImageNotExist()
    assert imageToDelete.save(flush:true)!=null
    String jsonImage = imageToDelete.encodeAsJSON()
    int idImage = imageToDelete.id
    log.info("delete image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+idImage+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();

    assertEquals(404,code)


    /*log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int newIdImage  = json.abstractimage.id



    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+newIdImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject


    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL+".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+newIdImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code) */


  }

  void testDeleteImageWithData()  {
    log.info("create image")
    def imageToDelete = BasicInstance.createOrGetBasicImageInstance()
    def annotation = BasicInstance.createOrGetBasicAnnotation()
    annotation.image = imageToDelete
    annotation.save(flush:true)
    String jsonImage = imageToDelete.encodeAsJSON()

    log.info("delete image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }

  void testDeleteImageNoExist()  {
    log.info("create image")
    def imageToDelete = BasicInstance.createOrGetBasicAbstractImage()
    String jsonImage = imageToDelete.encodeAsJSON()

    log.info("delete image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }

}