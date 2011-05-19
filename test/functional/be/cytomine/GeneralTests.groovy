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

}
