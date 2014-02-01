package be.cytomine

import be.cytomine.ontology.Relation
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 21/02/11
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
class RelationTests {

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
      assert 200==code
    def json = JSON.parse(response)
    assert json.collection instanceof JSONArray
  }


    void testShowRelationWithCredential() {

      log.info("show relation")
        Relation relation = BasicInstanceBuilder.getRelation()
      String URL = Infos.CYTOMINEURL+"api/relation/${relation.id}.json"
      HttpClient client = new HttpClient();
      client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
      client.get()
      int code  = client.getResponseCode()
      String response = client.getResponseData()
      client.disconnect();

      log.info("check response:"+response)
        assert 200==code
      def json = JSON.parse(response)
      assert json instanceof JSONObject
    }

    void testShowRelationWithCredentialNotExist() {

      log.info("show relation")
      String URL = Infos.CYTOMINEURL+"api/relation/-99.json"
      HttpClient client = new HttpClient();
      client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
      client.get()
      int code  = client.getResponseCode()
      String response = client.getResponseData()
      client.disconnect();

      log.info("check response:"+response)
        assert 404==code
    }
}
