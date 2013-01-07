package be.cytomine

import be.cytomine.project.Discipline
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.DisciplineAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.test.http.ProcessingServerAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class ProcessingServerTests extends functionaltestplugin.FunctionalTestCase {

  void testListProcessingServerWithCredential() {
      def result = ProcessingServerAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testShowProcessingServerWithCredential() {
      def result = ProcessingServerAPI.show(BasicInstance.createOrGetBasicProcessingServer().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }
}
