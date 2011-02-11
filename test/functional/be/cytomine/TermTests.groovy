package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance

import be.cytomine.test.Infos
import be.cytomine.project.Term
import org.apache.http.entity.ContentProducer
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.EntityTemplate
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.HttpResponse
import org.apache.commons.io.IOUtils
import org.apache.http.client.AuthCache
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.client.protocol.ClientContext
import org.apache.http.HttpHost
import be.cytomine.test.HttpClient
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class TermTests extends functionaltestplugin.FunctionalTestCase {

  void testGetAnnotationsWithCredential() {
    Term term =  BasicInstance.createOrGetBasicTerm()
    String URL = Infos.CYTOMINEURL+"api/term/"+term.id +".json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()

    client.disconnect();
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
  }

  void testGetAnnotationsWithoutCredential() {
   /* String URL = Infos.CYTOMINEURL+"api/term/1.json"
    HttpClient client = new HttpClient(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.connect("GET");
    int code  = client.getResponseCode()
    assertEquals(401,code)
    client.disconnect();  */
  }

  /* void testAddTermCorrect() {
  def termToAdd = BasicInstance.createOrGetBasicTerm()
  String jsonTerm = ([term : termToAdd]).encodeAsJSON()
  println "jsonTerm="+jsonTerm

  String URL = Infos.CYTOMINEURL+"api/term.json"
  HttpClient client = new HttpClient(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);

  client.connect("POST");
  //jsonTerm.toString()
  client.post(jsonTerm.toString())
  int code  = client.getResponseCode()
  println "code="+code
  String response = client.getResponseString()
  client.disconnect();
  assertEquals(201,code)
  def json = JSON.parse(response)
  assert json instanceof JSONObject
}  */

  void testGetTermHttp() {
    Term term =  BasicInstance.createOrGetBasicTerm()
    String URL = Infos.CYTOMINEURL+"api/term.json"
    be.cytomine.test.HttpClient client = new be.cytomine.test.HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println "Code="+code
  }

  void testPostTermHttp() {
    def termToAdd = BasicInstance.createOrGetBasicTerm()
    String jsonTerm = ([term : termToAdd]).encodeAsJSON()
        String URL = Infos.CYTOMINEURL+"api/term.json"
        be.cytomine.test.HttpClient client = new be.cytomine.test.HttpClient()
        client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        client.post(jsonTerm)

  }

}
