package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.test.http.AbstractImageGroupAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 22/02/11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class AbstractImageGroupTests extends functionaltestplugin.FunctionalTestCase {

  void testListAbstractImageGroupByAbstractImageWithCredential() {
    AbstractImage abstractimage = BasicInstance.createOrGetBasicAbstractImage()
    def result = AbstractImageGroupAPI.listByImage(abstractimage.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200, result.code)
    def json = JSON.parse(result.data)
    assert json instanceof JSONArray


      MyGrailsUtil.runWithRoles(["ROLE_1", "ROLE_2"]) {
          ... code to run with the given roles ...
      }

  }

  void testListAbstractImageGroupByAbstractImageWithAbstractImageNotExist() {
      def result = AbstractImageGroupAPI.listByImage(-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

    void testListAbstractImageGroupByGroupWithCredential() {
      Group group = BasicInstance.createOrGetBasicGroup()
      def result = AbstractImageGroupAPI.listByGroup(group.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
    }

    void testListAbstractImageGroupByGroupWithGroupNotExist() {
      def result = AbstractImageGroupAPI.listByGroup(-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(404, result.code)
    }

  void testGetAbstractImageGroupWithCredential() {
    def abstractimageGroupToAdd = BasicInstance.createOrGetBasicAbstractImageGroup()
      def result = AbstractImageGroupAPI.show(abstractimageGroupToAdd.abstractimage.id,abstractimageGroupToAdd.group.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200, result.code)
  }

  void testAddAbstractImageGroupCorrect() {
    def abstractimageGroupToAdd = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupCorrect")
    abstractimageGroupToAdd.discard()
    String json = abstractimageGroupToAdd.encodeAsJSON()
    def result = AbstractImageGroupAPI.create(abstractimageGroupToAdd.abstractimage.id,abstractimageGroupToAdd.group.id,json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
    assertEquals(200, result.code)
    result = AbstractImageGroupAPI.show(abstractimageGroupToAdd.abstractimage.id,abstractimageGroupToAdd.group.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
    assertEquals(200, result.code)
  }

   void testAddAbstractImageGroupAlreadyExist() {
    def abstractimageGroupToAdd = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupAlreadyExist")
    abstractimageGroupToAdd.save(flush:true)
    abstractimageGroupToAdd.discard()
    String json = abstractimageGroupToAdd.encodeAsJSON()
     def result = AbstractImageGroupAPI.create(abstractimageGroupToAdd.abstractimage.id,abstractimageGroupToAdd.group.id,json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
     assertEquals(409, result.code)
  }

    void testAddAbstractImageGroupWithAbstractImageNotExist() {
      def abstractimageGroupToAdd = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupCorrect")
      abstractimageGroupToAdd.discard()
      def jsonUpdate = JSON.parse(abstractimageGroupToAdd.encodeAsJSON())
      jsonUpdate.abstractimage = -99
      def result = AbstractImageGroupAPI.create(-99,abstractimageGroupToAdd.group.id,jsonUpdate.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
    }

    void testAddAbstractImageGroupWithGroupNotExist() {
      def abstractimageGroupToAdd = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupCorrect")
      abstractimageGroupToAdd.discard()
      def jsonUpdate = JSON.parse(abstractimageGroupToAdd.encodeAsJSON())
      jsonUpdate.group = -99
      def result = AbstractImageGroupAPI.create(abstractimageGroupToAdd.abstractimage.id,-99,jsonUpdate.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
    }

    void testDeleteAbstractImageGroup() {
        def abstractimageGroupToDelete = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupCorrect")
        assert abstractimageGroupToDelete.save(flush: true)  != null
        def result = AbstractImageGroupAPI.delete(abstractimageGroupToDelete.abstractimage.id,abstractimageGroupToDelete.group.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def showResult = AbstractImageGroupAPI.show(abstractimageGroupToDelete.abstractimage.id,abstractimageGroupToDelete.group.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)        
    }

    void testDeleteAbstractImageGroupNotExist() {
        def abstractimageGroupToDelete = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupCorrect")
        assert abstractimageGroupToDelete.save(flush: true)  != null
        def result = AbstractImageGroupAPI.delete(-99,-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }
}
