package be.cytomine.project

class AnnotationTerm {

  Annotation annotation
  Term term

  static AnnotationTerm link(Annotation annotation,Term term) {
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation, term)
    if (!annotationTerm) {
      annotationTerm = new AnnotationTerm()
      annotation?.addToAnnotationTerm(annotationTerm)
      term?.addToAnnotationTerm(annotationTerm)
      annotationTerm.save()
    }
    return annotationTerm
  }

  static void unlink(Annotation annotation, Term term) {
    def annotationTerm = AnnotationTerm.findByAnnotationAndTerm(annotation, term)
    if (annotationTerm) {
      annotation?.removeFromAnnotationTerm(annotationTerm)
      term?.removeFromAnnotationTerm(annotationTerm)
      annotationTerm.delete()
    }

  }
}
