package be.cytomine.marshallers


import be.cytomine.project.Annotation

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 4/02/11
 * Time: 14:12
 */
class Marshallers {

  public static void init() {
    Annotation.registerMarshaller();
  }
}
