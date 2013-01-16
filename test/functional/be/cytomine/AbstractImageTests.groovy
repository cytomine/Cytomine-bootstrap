package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.security.User
import be.cytomine.utils.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.AbstractImageAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.utils.UpdateData
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServer
import be.cytomine.image.AbstractImageService
import be.cytomine.ontology.UserAnnotation
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import cytomine.web.ImagePropertiesService
import be.cytomine.image.AbstractImageGroup
import be.cytomine.security.Group

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 13:49
 * To change this template use File | Settings | File Templates.
 */
class AbstractImageTests extends functionaltestplugin.FunctionalTestCase{


    def oldMethod2
    def oldMethod3

    void setUp()  {

        oldMethod2 = AbstractImageService.metaClass.metadata
        AbstractImageService.metaClass.metadata = { def id ->
           return "metdatatext"
        }
        oldMethod3 = ImagePropertiesService.metaClass.extractUseful
        ImagePropertiesService.metaClass.extractUseful = { AbstractImage img ->
        }




    }

    void tearDown() {
        AbstractImage.metaClass.metadata = oldMethod2
        ImagePropertiesService.metaClass.extractUseful = oldMethod3
    }


  void testListImages() {
      BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

    void testListImagesDatatable() {
        def image = BasicInstance.createOrGetBasicAbstractImage()
        AbstractImageGroup.link(image,Group.findByName(Infos.GOODLOGIN))
        def result = AbstractImageAPI.list(true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

  void testListImagesWithoutCredential() {
      BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
      assertEquals(401, result.code)
  }

  void testListAnnotationsByProject() {
      BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.listByProject(BasicInstance.createOrGetBasicProject().id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListAnnotationsByProjectNoExistWithCredential() {
      def result = AbstractImageAPI.listByProject(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testGetImageWithCredential() {
      AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.show(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }


  void testGetMetadata() {
      AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
      def result = AbstractImageAPI.getMetadata(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
  }

    void testGetImageProperties() {
        AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
        if(image.imageProperties==null || image.imageProperties.isEmpty()) {
            ImageProperty imageProperty = new ImageProperty(key: "key1", value: "value1",image:image)
            BasicInstance.saveDomain(imageProperty)
            image.addToImageProperties(imageProperty)
            BasicInstance.saveDomain(image)
        }
        def result = AbstractImageAPI.getInfo(image.id,"property",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert json.size()>0
        assert json[0].key.equals(image.imageProperties.first().key)
    }

    void testGetImageProperty() {
        AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
        if(image.imageProperties==null || image.imageProperties.isEmpty()) {
            ImageProperty imageProperty = new ImageProperty(key: "key1", value: "value1",image:image)
            BasicInstance.saveDomain(imageProperty)
            image.addToImageProperties(imageProperty)
            BasicInstance.saveDomain(image)
        }
        println "==>"+ImageProperty.list().collect{it.id}
        def result = AbstractImageAPI.getInfo(image.id,"property/${image.imageProperties.first().id}",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.key.equals(image.imageProperties.first().key)
    }

    void testGetImagePropertyNotFound() {
        AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
        def result = AbstractImageAPI.getInfo(image.id,"property/-99",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testGetImageServers() {
        AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
        def result = AbstractImageAPI.getInfo(image.id,"imageservers",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }


    void testGetThumb() {
        println "AbstractImage.metaClass=${AbstractImage.metaClass}"
        println "AbstractImage.metaClass.getImageServers=${AbstractImage.metaClass.getImageServers}"
//        def oldMethod1 = AbstractImage.metaClass.getImageServers
//        AbstractImage.metaClass.getImageServers  = {->
//            println "### mocking method"
//            ImageServer bidon = BasicInstance.createOrGetBasicImageServer()
//            return [bidon]
//        }

        def oldMethod1 = AbstractImage.metaClass.getThumbURL
        AbstractImage.metaClass.getThumbURL  = {->
            println "### mocking method"
            return "http://upload.wikimedia.org/wikipedia/commons/6/63/Wikipedia-logo.png"
        }

        AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
        def result = AbstractImageAPI.getInfo(image.id,"thumb",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        //PROB LIE AU SETUP METHOD
        AbstractImage.metaClass.getThumbURL = oldMethod1
    }

    void testGetPreview() {
        AbstractImage image = BasicInstance.createOrGetBasicAbstractImage()
        def result = AbstractImageAPI.getInfo(image.id,"preview",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testGetAnnotationCrop() {
        UserAnnotation annotation = BasicInstance.createOrGetBasicUserAnnotation()
        def result = AbstractImageAPI.getCrop(annotation.id,"annotation",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testGetUserAnnotationCrop() {
        UserAnnotation annotation = BasicInstance.createOrGetBasicUserAnnotation()
        def result = AbstractImageAPI.getCrop(annotation.id,"userannotation",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testGetAlgoAnnotationCrop() {
        AlgoAnnotation annotation = BasicInstance.createOrGetBasicAlgoAnnotation()
        def result = AbstractImageAPI.getCrop(annotation.id,"algoannotation",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testGetReviewedAnnotationCrop() {
        ReviewedAnnotation annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = AbstractImageAPI.getCrop(annotation.id,"reviewedannotation",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
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
      def data = UpdateData.createUpdateSet(imageToAdd)
      def result = AbstractImageAPI.update(data.oldData.id,data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int id = json.abstractimage.id
      def showResult = AbstractImageAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstance.compareAbstractImage(data.mapNew, json)
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