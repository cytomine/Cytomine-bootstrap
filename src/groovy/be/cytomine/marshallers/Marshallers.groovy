package be.cytomine.marshallers


import be.cytomine.project.Annotation
import be.cytomine.security.User
import be.cytomine.project.Project
import grails.converters.JSON
import be.cytomine.project.Slide

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 4/02/11
 * Time: 14:12
 */
class Marshallers {

  public static void init() {
    Annotation.registerMarshaller();
    User.registerMarshaller();
    Project.registerMarshaller();
    //Slide.registerMarshaller();

    println "Register custom JSON renderer for " + Slide.class
    JSON.registerObjectMarshaller(Slide) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['scan'] = it.scan
      return returnArray
    }

  }
}
