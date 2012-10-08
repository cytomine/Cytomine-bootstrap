package be.cytomine

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 21/02/11
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
class RelationTests extends functionaltestplugin.FunctionalTestCase{

  void testListRelationWithCredential() {

    log.info("get relation")
    String URL = Infos.CYTOMINEURL+"api/relation.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response:"+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray
  }
}
