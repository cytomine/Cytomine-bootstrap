package be.cytomine.project
import grails.converters.JSON
class AnnotationTerm {

  Annotation annotation
  Term term

  /*static mapping = {
    version false
  }           */

  static AnnotationTerm link(Annotation annotation,Term term) {
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation, term)
    if (!annotationTerm) {
      annotationTerm = new AnnotationTerm()
      annotation?.addToAnnotationTerm(annotationTerm)
      term?.addToAnnotationTerm(annotationTerm)
      annotationTerm.save(flush : true)
    }
    return annotationTerm
  }

  static AnnotationTerm link(long id,Annotation annotation,Term term) {
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation, term)
    if (!annotationTerm) {
      annotationTerm = new AnnotationTerm()
      annotationTerm.id = id
      annotation?.addToAnnotationTerm(annotationTerm)
      term?.addToAnnotationTerm(annotationTerm)
      annotationTerm.save(flush:true)
    }
    return annotationTerm
  }

  static void unlink(Annotation annotation, Term term) {
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation, term)
    println "unlink annotationTerm="+annotationTerm
    if (annotationTerm) {
      annotation?.removeFromAnnotationTerm(annotationTerm)
      term?.removeFromAnnotationTerm(annotationTerm)
      println "unlink delete annotationTerm.id="+annotationTerm.id
      annotationTerm.delete(flush : true)
    }

  }

  static AnnotationTerm createAnnotationTermFromData(jsonAnnotationTerm) {
    def annotationTerm = new AnnotationTerm()
    getAnnotationTermFromData(annotationTerm,jsonAnnotationTerm)
  }

  static AnnotationTerm getAnnotationTermFromData(annotationTerm,jsonAnnotationTerm) {
    //TODO: check constraint
    println "jsonAnnotationTerm from getAnnotationTermFromData = " + jsonAnnotationTerm
    
    String annotationId = jsonAnnotationTerm.annotation.id.toString()
    println "annotationId =" + annotationId
    if(!annotationId.equals("null"))
      annotationTerm.annotation = Annotation.get(annotationId)
    if(annotationTerm.annotation==null) throw new IllegalArgumentException("Annotation was not found with id:"+ annotationId)
    
    
    String termId = jsonAnnotationTerm.term.id.toString()
    if(!termId.equals("null"))
      annotationTerm.term = Term.get(termId)
    if(annotationTerm.term==null) throw new IllegalArgumentException("Term was not found with id:"+ termId)

    return annotationTerm;
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + AnnotationTerm.class
    JSON.registerObjectMarshaller(AnnotationTerm) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['annotation'] = it.annotation
      returnArray['term'] = it.term
      return returnArray
    }
  }
}
