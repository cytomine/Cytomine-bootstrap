package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.ontology.Annotation
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.security.User
import be.cytomine.image.AbstractImage
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.image.ImageInstance
import be.cytomine.command.Command

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class GeneralTests extends functionaltestplugin.FunctionalTestCase {

  void testCommandMaxSizeTooLong() {

    log.info("create image")
    String jsonImage = "{\"text\" : \"*************************************************************************"
    String textAdded = "***************************************************************************************"
    textAdded = textAdded+textAdded+textAdded+textAdded+textAdded+textAdded+textAdded+textAdded+textAdded+textAdded
    //create a big string (don't care about content)
    while(jsonImage.size()<=(Command.MAXSIZEREQUEST*2))
    {
      jsonImage+=textAdded
    }
    jsonImage=jsonImage+"\"}"

    log.info("post with data size:"+jsonImage.size())
    String URL = Infos.CYTOMINEURL+"api/image.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(413,code)
    def json = JSON.parse(response)
  }

  void testCommandMaxSizeOK() {

    log.info("create image")
    String jsonImage = "{\"text\" : \"*************************************************************************"
    String textAdded = "***************************************************************************************"
    jsonImage=jsonImage+"\"}"

    log.info("post with data size:"+jsonImage.size())
    String URL = Infos.CYTOMINEURL+"api/image.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(true,code!=413)
    def json = JSON.parse(response)
  }


  void testLastAction() {

    /*
     * Add an annotation, undo and redo it
     */
    log.info("create annotation")
    def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    String jsonAnnotation = annotationToAdd.encodeAsJSON()

    log.info("post annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idAnnotation = json.annotation.id

    log.info("check if object "+ idAnnotation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idAnnotation +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(404,code)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(201,code)

    /*
     * Get the last 3 commands: it must be "REDO ADD ANNOTATION", "UNDO ADD ANNOTATION" and "ADD ANNOTATION"
     */
    Long idProject =  annotationToAdd.image.project.id
    Integer max = 3
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +"/last/"+ max +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONArray

    log.info("response="+response);

  }
}
