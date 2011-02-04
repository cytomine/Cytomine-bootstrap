package be.cytomine.command

import grails.test.*
import be.cytomine.project.Annotation
import grails.converters.JSON
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.command.annotation.EditAnnotationCommand

class EditAnnotationCommandTests extends GroovyTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testExecuteEditAnnotation() {
      /*String newGeom = "POINT(1000 1000)"
      Annotation annotationToAdd = Annotation.createOrGetBasicAnnotation()
      Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
      annotationToEdit.location = new WKTReader().read(newGeom)

      def jsonAnnotation = Annotation.convertToMap(annotationToEdit) as JSON;
      println "jsonAnnotation="+jsonAnnotation.toString();
      Command editAnnotationCommand = new EditAnnotationCommand(postData : jsonAnnotation.toString())
      //edit annotation
      def result = editAnnotationCommand.execute()
      //test if response is ok
      assertEquals(201,result.status)
      Annotation annotation = result.data.annotation
      assertTrue("Annotation result is not a correct annotation", (annotation instanceof Annotation))

      //test if exist and is equal
      def newAnnotation = Annotation.get(annotation.id)

      assertNotNull("Annotation is not in database", newAnnotation)
      assertEquals("Annotation geom is not modified",newGeom.replace(' ', ''),newAnnotation.location.toString().replace(' ',''))
      assertFalse("Annotation was not modified",annotationToAdd.equals(newAnnotation))    */
    }

    void testExecuteEditAnnotationNotExist() {

    }

    void testExecuteEditAnnotationWithBadGeom() {

    }
}
