package be.cytomine.project

import grails.converters.JSON

class Term {

  String name
  String comment

  static belongsTo = Annotation
  static hasMany = [ child : Term, annotationTerm:AnnotationTerm]

    static constraints = {
    }

   static void registerMarshaller() {
    println "Register custom JSON renderer for " + Annotation.class
    JSON.registerObjectMarshaller(Annotation) {
      def returnArray = [:]
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['comment'] = it.comment
      return returnArray
    }
  }

}
