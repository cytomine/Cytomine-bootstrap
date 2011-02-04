package be.cytomine.command

import grails.test.*
import be.cytomine.project.Scan
import net.sf.json.JSONObject
import be.cytomine.project.Annotation
import grails.converters.JSON

class AddAnnotationCommandTests extends GroovyTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSomething() {

    }

    void testExecuteAddAnnotation()
    {
      Scan scan = Scan.createOrGetBasicScan()
      String req = "{\"annotation\":{\"location\":\"POINT(17573.5 21853.5)\",\"name\":\"test\",\"class\":\"be.cytomine.project.Annotation\",\"scan\":"+scan.id+"}}"
      Command addAnnotationCommand = new AddAnnotationCommand(postData : req)
      //add annotation
      def result = addAnnotationCommand.execute()

      //test if response is ok
      assertEquals(201,result.status)

      Annotation annotation = result.data.annotation
      assert annotation instanceof Annotation
      assertEquals("POINT(17573.5 21853.5)".replaceAll(' ',''),annotation.location.toString().replaceAll(' ',''))

      //test if exist
      def newAnnotation = Annotation.get(annotation.id)
      assertNotNull newAnnotation
    }


   void testExecuteAddAnnotationWithBadGeom()
   {


   }

   void testExecuteAddAnnotationWithScanNotExist()
   {


   }


}
