package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.test.http.ImageFilterAPI
import be.cytomine.test.http.ImageFilterProjectAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class ImageFilterTests extends functionaltestplugin.FunctionalTestCase {

  void testListImageFilterWithCredential() {
      def result = ImageFilterAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testShowImageFilterWithCredential() {
      def result = ImageFilterAPI.show(BasicInstance.createOrGetBasicImageFilter().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject

      result = ImageFilterAPI.show(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  /*
    Image filter project
  */
    void testListImageFilterProject() {
        def result = ImageFilterProjectAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testAddImageFilterProject() {
        def ifp = BasicInstance.getBasicImageFilterProjectNotExist()
        def result = ImageFilterProjectAPI.create(ifp.encodeAsJSON(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testDeleteImageFilterProject() {
       def ifp = BasicInstance.getBasicImageFilterProjectNotExist()
       ifp.save(flush: true)
        def result = ImageFilterProjectAPI.delete(ifp.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }



}
