package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
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
class ImageFilterTests  {

  void testListImageFilterWithCredential() {
      def result = ImageFilterAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testShowImageFilterWithCredential() {
      def result = ImageFilterAPI.show(BasicInstanceBuilder.getImageFilter().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject

      result = ImageFilterAPI.show(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

  /*
    Image filter project
  */
    void testListImageFilterProject() {
        def result = ImageFilterProjectAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testAddImageFilterProject() {
        def ifp = BasicInstanceBuilder.getImageFilterProjectNotExist()
        def result = ImageFilterProjectAPI.create(ifp.encodeAsJSON(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testDeleteImageFilterProject() {
       def ifp = BasicInstanceBuilder.getImageFilterProjectNotExist()
       ifp.save(flush: true)
        def result = ImageFilterProjectAPI.delete(ifp.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }



}
