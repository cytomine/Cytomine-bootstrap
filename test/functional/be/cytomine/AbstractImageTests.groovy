package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.image.server.Storage
import be.cytomine.ontology.Property
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AbstractImageAPI
import be.cytomine.utils.UpdateData
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
class AbstractImageTests {

  void testListImages() {
      Storage storage = BasicInstanceBuilder.getStorage()
      BasicInstanceBuilder.getAbstractImage()
      def result = AbstractImageAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

    void testListImagesDatatable() {
        def result = AbstractImageAPI.list(true,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

  void testListImagesWithoutCredential() {
      BasicInstanceBuilder.getAbstractImage()
      def result = AbstractImageAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
      assert 401 == result.code
  }

  void testListAnnotationsByProject() {
      BasicInstanceBuilder.getAbstractImage()
      def result = AbstractImageAPI.listByProject(BasicInstanceBuilder.getProject().id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testListAnnotationsByProjectNoExistWithCredential() {
      def result = AbstractImageAPI.listByProject(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  void testGetImageWithCredential() {
      AbstractImage image = BasicInstanceBuilder.getAbstractImage()
      def result = AbstractImageAPI.show(image.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }




    void testGetImageProperties() {
        AbstractImage image = BasicInstanceBuilder.getAbstractImage()
        if(!Property.findByDomainIdent(image.id)) {
            Property imageProperty = new Property(key: "key1", value: "value1",domainIdent:image.id, domainClassName:image.class.name)
            BasicInstanceBuilder.saveDomain(imageProperty)
        }
        def result = AbstractImageAPI.getProperty(image.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()>0
    }

    void testGetImageServers() {
        AbstractImage image = BasicInstanceBuilder.getAbstractImage()
        def result = AbstractImageAPI.getInfo(image.id,"imageservers",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

  void testAddImageCorrect() {
      def imageToAdd = BasicInstanceBuilder.getAbstractImageNotExist()
      String json = imageToAdd.encodeAsJSON()
      println "encodeAsJSON="+json
      def result = AbstractImageAPI.create(json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      int id = result.data.id
//      result = AbstractImageAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
//      assert 200 == result.code
  }

  void testaddImageWithUnexistingScanner() {
      def imageToAdd = BasicInstanceBuilder.getAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.scanner = -99
      def result = AbstractImageAPI.create(json.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 400 == result.code
  }

  void testaddImageWithUnexistingSlide() {
      def imageToAdd = BasicInstanceBuilder.getAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.sample = -99
      def result = AbstractImageAPI.create(json.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 400 == result.code
  }

  void testaddImageWithUnexistingMime() {
      def imageToAdd = BasicInstanceBuilder.getAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.mime = -99
      def result = AbstractImageAPI.create(json.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 400 == result.code
  }

  void testaddImageWithUnexistingImageServer() {
    def mime = BasicInstanceBuilder.getMimeNotExist()
      def imageToAdd = BasicInstanceBuilder.getAbstractImageNotExist()
      def json = JSON.parse((String)imageToAdd.encodeAsJSON())
      json.mime = mime.id
      def result = AbstractImageAPI.create(json.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 400 == result.code
  }

  void testEditImage() {
      def image = BasicInstanceBuilder.getAbstractImage()
      def data = UpdateData.createUpdateSet(
              image,
                    [height:[10000,900000],
                      width:[1000,9000],
                      path:["OLDPATH","NEWPATH"],
                      scanner:[BasicInstanceBuilder.getScanner(),BasicInstanceBuilder.getNewScannerNotExist(true)],
                      filename: ["OLDNAME","NEWNAME"]]
      )

      def result = AbstractImageAPI.update(image.id,data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int id = json.abstractimage.id
      def showResult = AbstractImageAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstanceBuilder.compare(data.mapNew, json)
  }

  void testDeleteImage()  {
      def imageToDelete = BasicInstanceBuilder.getAbstractImage()
      Long id = imageToDelete.id
      def result = AbstractImageAPI.delete(id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      println "testDeleteImage=" +result
      assert 200 == result.code
      result = AbstractImageAPI.show(id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.deleted != null
      assert json.deleted != ""
  }

  void testDeleteImageWithData()  {
    def imageToDelete = BasicInstanceBuilder.getImageInstance()
    def annotation = BasicInstanceBuilder.getUserAnnotation()
    annotation.image = imageToDelete
    annotation.save(flush:true)
      Long id = imageToDelete.baseImage.id
      def result = AbstractImageAPI.delete(id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 403 == result.code
  }

  void testDeleteImageNoExist()  {
      def result = AbstractImageAPI.delete(-99,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

}