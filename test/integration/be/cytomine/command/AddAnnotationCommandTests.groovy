package be.cytomine.command

import be.cytomine.project.Annotation

import be.cytomine.command.annotation.AddAnnotationCommand

import grails.converters.JSON
import be.cytomine.marshallers.Marshallers
import be.cytomine.test.BasicInstance

class AddAnnotationCommandTests extends GroovyTestCase {
  protected void setUp() {
    super.setUp()
    Marshallers.init();
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testExecuteAddAnnotation()
  {
    Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    def jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()
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
    assertEquals("Annotation add and get are different",annotation,newAnnotation)

    //test if unod work
    addAnnotationCommand.undo()
    newAnnotation = Annotation.get(annotation.id)
    assertNull("Annotation is in database", newAnnotation)

    //test if redo work
    addAnnotationCommand.redo()
    newAnnotation = Annotation.get(annotation.id)
    assertNotNull("Annotation is not in database", newAnnotation)
    assertEquals("Annotation add and get are different",annotation,newAnnotation)
  }


  void testExecuteAddAnnotationWithBadGeom()
  {
    Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    def jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()
    def updateAnnotation = JSON.parse(jsonAnnotation)
    updateAnnotation.annotation.location = 'POINT(BAD GEOMETRY)'
    jsonAnnotation = updateAnnotation.encodeAsJSON()

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
    Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    def jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()
    def updateAnnotation = JSON.parse(jsonAnnotation)
    updateAnnotation.annotation.image = -99
    jsonAnnotation = updateAnnotation.encodeAsJSON()

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
