package be.cytomine.marshallers


import be.cytomine.project.Annotation
import be.cytomine.security.User
import be.cytomine.project.Project
import grails.converters.JSON
import be.cytomine.project.Slide
import be.cytomine.project.Term
import be.cytomine.project.Image

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 4/02/11
 * Time: 14:12
 */
class Marshallers {

  public static void init() {
    Term.registerMarshaller();
    Annotation.registerMarshaller();
    User.registerMarshaller();
    Project.registerMarshaller();
    Slide.registerMarshaller();
    Image.registerMarshaller();


  }
}
