package be.cytomine.api.project

import grails.test.*

import be.cytomine.project.Image

import be.cytomine.project.Annotation

import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.marshallers.Marshallers

class RestAnnotationControllerTests extends GrailsUnitTestCase {



  protected void setUp() {
    super.setUp()
    Marshallers.init();
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testShowAnnotation() {
    String name = "name"
    String location = "POINT (1000 1000)"

    def scan = Image.createOrGetBasicScan()
    def annotation = new Annotation(name:name,location:new WKTReader().read(location),image: scan);
    assertTrue(annotation.validate())
    annotation.save(flush : true)

    RestAnnotationController c = new RestAnnotationController()
    c.params.id = annotation.id
    c.show()

    def jsonAnnotation = c.response.contentAsString
    println  jsonAnnotation
    def json = JSON.parse(jsonAnnotation);
    //{"annotation":{"id":4,"name":"name","location":"POINT (1000 1000)","image":119}}

    assert json instanceof JSONObject // In this case, JSON.parse returns a JSONObject instance
    assert json instanceof Map // which implements the Map interface

    assertEquals(name,json.annotation.name)// access a property
    assertEquals(location, json.annotation.location)

  }

  void testShowAnnotationNotExist() {

    RestAnnotationController c = new RestAnnotationController()
    c.params.id = -99
    c.show()
    def code = c.response.status
    println code
    assertEquals(404,code)
  }

  void testGoogleAccess() {
    URL url =  new URL("http://www.google.com")
    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    con.setRequestMethod("GET");
    con.connect();
    String reponse=con.getResponseCode();
    println reponse; // test affiche
    assertEquals("200",reponse)
  }

}
