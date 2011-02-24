package be.cytomine.project
import grails.converters.JSON
import be.cytomine.SequenceDomain

class AnnotationTerm implements Serializable{

  Annotation annotation
  Term term

  String toString()
  {
    "[" + this.id + " <" + annotation + "," + term + ">]"
  }

  static AnnotationTerm link(Annotation annotation,Term term) {
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation, term)
    if (!annotationTerm) {
      annotationTerm = new AnnotationTerm()
      annotation?.addToAnnotationTerm(annotationTerm)
      term?.addToAnnotationTerm(annotationTerm)
      println "save annotationTerm"
      annotationTerm.save(flush : true)
    } else throw new IllegalArgumentException("Annotation " + annotation.id + " and term " + term.id + " are already mapped")
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
    } else throw new IllegalArgumentException("Annotation " + annotation.id + " and term " + term.id + " are already mapped")
    return annotationTerm
  }

  static void unlink(Annotation annotation, Term term) {
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation, term)

    println "unlink annotationTerm="+annotationTerm
    if (annotationTerm) {
        annotation?.removeFromAnnotationTerm(annotationTerm)
        term?.removeFromAnnotationTerm(annotationTerm)
      annotationTerm.delete(flush : true)

    }
  }

  static AnnotationTerm createAnnotationTermFromData(jsonAnnotationTerm) {
    def annotationTerm = new AnnotationTerm()
    getAnnotationTermFromData(annotationTerm,jsonAnnotationTerm)
  }

  static AnnotationTerm getAnnotationTermFromData(annotationTerm,jsonAnnotationTerm) {
    println "jsonAnnotationTerm from getAnnotationTermFromData = " + jsonAnnotationTerm
    annotationTerm.annotation = Annotation.get(jsonAnnotationTerm.annotation.id.toString())
    annotationTerm.term = Term.get(jsonAnnotationTerm.term.id.toString())
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
