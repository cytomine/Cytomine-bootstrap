package be.cytomine.project

import grails.converters.JSON
import be.cytomine.SequenceDomain

class Slide extends SequenceDomain {

  String name
  int order

  static mapping = {
    columns {
      order column:"`order`"  //otherwise there is a conflict with the word "ORDER" from the SQL SYNTAX
    }
  }

  String toString() {
    name
  }

  static hasMany = [projectSlide:ProjectSlide, image:Image]

  static constraints = {
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Slide.class
    JSON.registerObjectMarshaller(Slide) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      //returnArray['image'] = it.image
      return returnArray
    }
  }
}
