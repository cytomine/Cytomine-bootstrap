package be.cytomine.project

import grails.converters.JSON
import be.cytomine.SequenceDomain

class Term {

  String name
  String comment


  static belongsTo = Annotation
  static hasMany = [ child : Term, annotationTerm:AnnotationTerm, termOntology: TermOntology ]

    static constraints = {
      comment(blank:true,nullable:true)
    }

  def ontologies() {
    return annotationTerm.collect{it.term}
   }

  static Term getTermFromData(data) {
    def term = new Term()
    term.name = data.term.name
    term.comment = data.term.comment
    //TODO: implement children&co
    return term;
  }


   static void registerMarshaller() {
    println "Register custom JSON renderer for " + Term.class
    JSON.registerObjectMarshaller(Term) {
      def returnArray = [:]
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['comment'] = it.comment

      def children = [:]
      it.child.each { child ->
        def childArray = [:]
        childArray['name'] = child.name
        children[child.id] = childArray
      }
      returnArray['children'] = children
      return returnArray
    }
  }

}
