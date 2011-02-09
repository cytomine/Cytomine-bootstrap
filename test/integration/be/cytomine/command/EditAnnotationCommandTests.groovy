package be.cytomine.command

import be.cytomine.project.Annotation
import grails.converters.JSON
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.command.annotation.EditAnnotationCommand
import be.cytomine.marshallers.Marshallers
import be.cytomine.test.BasicInstance

class EditAnnotationCommandTests extends GroovyTestCase {
    protected void setUp() {
        super.setUp()
      Marshallers.init();
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testExecuteEditAnnotation() {
      String oldGeom = "POINT (1111 1111)"
      String newGeom = "POINT (9999 9999)"

      /* Create a old annotation with point 1111 1111 */
      Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
      annotationToAdd.location =  new WKTReader().read(oldGeom)
      annotationToAdd.save()

      /* Encode a niew annotation with point 9999 9999 */
      Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
      def json = [annotation : annotationToEdit]
      def jsonAnnotation = json.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonAnnotation)
      jsonUpdate.annotation.location = newGeom
      jsonAnnotation = jsonUpdate.encodeAsJSON()
      println "jsonAnnotation="+jsonAnnotation.toString();

      /* Call command to update POINT 1111 1111 => 9999 9999 */
      Command editAnnotationCommand = new EditAnnotationCommand(postData : jsonAnnotation.toString())
      def result = editAnnotationCommand.execute()
      assertEquals(200,result.status)
      Annotation annotation = result.data.annotation
      assertTrue("Annotation result is not a correct annotation", (annotation instanceof Annotation))

      /* Test if exist and is equal */
       println "annotation.id=" +annotation.id
      def newAnnotation = Annotation.get(annotation.id)
        println "annotation.location=" +newAnnotation.location

      assertNotNull("Annotation is not in database", newAnnotation)
      assertEquals("Annotation geom is not modified",newGeom.replace(' ', ''),newAnnotation.location.toString().replace(' ',''))

       /* Test if undo work and is equal to old annotation */
      editAnnotationCommand.undo()
      newAnnotation = Annotation.get(annotation.id)
      println "annotation.location=" +newAnnotation.location
      assertEquals("Annotation undo don't work",oldGeom.replace(' ', ''),newAnnotation.location.toString().replace(' ',''))

      /* Test if redo work and is equal to old annotation */
      editAnnotationCommand.redo()
      newAnnotation = Annotation.get(annotation.id)
      assertEquals("Annotation redo don't work",newGeom.replace(' ', ''),newAnnotation.location.toString().replace(' ',''))
    }

    void testExecuteEditAnnotationNotExist() {

      /* Create a old annotation */
      Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()

      /* Encode a niew annotation with point 9999 9999 */
      Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
      def json = [annotation : annotationToEdit]
      def jsonAnnotation = json.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonAnnotation)
      jsonUpdate.annotation.id = -99
      jsonAnnotation = jsonUpdate.encodeAsJSON()

      println "jsonAnnotation="+jsonAnnotation.toString();

      /* Call command to update POINT 1111 1111 => 9999 9999 */
      Command editAnnotationCommand = new EditAnnotationCommand(postData : jsonAnnotation.toString())
      def result = editAnnotationCommand.execute()
      assertEquals(404,result.status)
    }

    void testExecuteEditAnnotationWithBadGeom() {
       String oldGeom = "POINT (1111 1111)"
      String newGeom = "POINT (BAD GEOMETRY)"

      /* Create a old annotation with point 1111 1111 */
      Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
      annotationToAdd.location =  new WKTReader().read(oldGeom)
      annotationToAdd.save()

      /* Encode a niew annotation with bad point */
      Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
      def json = [annotation : annotationToEdit]
      def jsonAnnotation = json.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonAnnotation)
      jsonUpdate.annotation.location = newGeom
      jsonAnnotation = jsonUpdate.encodeAsJSON()
      println "jsonAnnotation="+jsonAnnotation.toString();

      /* Call command to update POINT 1111 1111 => bad point */
      Command editAnnotationCommand = new EditAnnotationCommand(postData : jsonAnnotation.toString())
      def result = editAnnotationCommand.execute()
      assertEquals(400,result.status)

      /* Test if exist and is equal to 1111 1111 and not bad point */
       //println "annotation.id=" +annotation.id
      def newAnnotation = Annotation.get(annotationToAdd.id)
      println "annotation.location=" +newAnnotation.location

      assertNotNull("Annotation is not in database", newAnnotation)
      assertEquals("Annotation shouldn't be modified",newAnnotation,annotationToAdd)

    }
}
