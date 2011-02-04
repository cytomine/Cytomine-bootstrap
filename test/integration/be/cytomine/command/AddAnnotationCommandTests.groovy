package be.cytomine.command

import be.cytomine.project.Scan

import be.cytomine.project.Annotation

import be.cytomine.command.annotation.AddAnnotationCommand
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON

class AddAnnotationCommandTests extends GroovyTestCase {
  protected void setUp() {
    super.setUp()
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testExecuteAddAnnotation()
  {
    Annotation annotationToAdd = Annotation.createOrGetBasicAnnotation()
    def jsonAnnotation = Annotation.convertToMap(annotationToAdd) as JSON;
    println "jsonAnnotation="+jsonAnnotation.toString();
    Command addAnnotationCommand = new AddAnnotationCommand(postData : jsonAnnotation.toString())
    //add annotation
    def result = addAnnotationCommand.execute()
    //test if response is ok
    assertEquals(201,result.status)
    Annotation annotation = result.data.annotation
    assertTrue("Annotation result is not a correct annotation", (annotation instanceof Annotation))

    //test if exist and is equal
    def newAnnotation = Annotation.get(annotation.id)
    assertNotNull("Annotation is not in database", newAnnotation)
    assertTrue("Annotation add and get are different",newAnnotation.equals(annotation))
  }


  void testExecuteAddAnnotationWithBadGeom()
  {
    Annotation annotationToAdd = Annotation.createOrGetBasicAnnotation()
    def obj =  Annotation.convertToMap(annotationToAdd) as JSON
    def jsonAnnotation = JSON.parse(obj.toString())
    println "jsonAnnotation="+jsonAnnotation.toString();
    jsonAnnotation.annotation.location = 'POINT(BAD GEOMETRY)'
    println "jsonAnnotation="+jsonAnnotation.toString();
    Command addAnnotationCommand = new AddAnnotationCommand(postData : jsonAnnotation.toString())
    //add annotation
    def result = addAnnotationCommand.execute()
    //test if response is ok
    assertEquals("Annotation with bad geometry must throw an error",400,result.status)
    assertNull("Annotation response with bad geometry must be null",result.data.annotation)

    println(result.data.errors)

  }

  void testExecuteAddAnnotationWithScanNotExist()
  {
    Annotation annotationToAdd = Annotation.createOrGetBasicAnnotation()
    def obj =  Annotation.convertToMap(annotationToAdd) as JSON
    println obj.toString()
    def jsonAnnotation = JSON.parse(obj.toString())
    jsonAnnotation.annotation.scan = -99
    println "jsonAnnotation="+jsonAnnotation.toString();
    Command addAnnotationCommand = new AddAnnotationCommand(postData : jsonAnnotation.toString())
    //add annotation
    def result = addAnnotationCommand.execute()
    //test if response is ok
    assertEquals(400,result.status)

    //test if not exist
    assertNull ("Annotation with bad scan id shouln't be saved", Annotation.get(result.data.annotation.id))

  }
}
