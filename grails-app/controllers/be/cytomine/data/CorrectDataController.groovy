package be.cytomine.data

import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/10/11
 * Time: 10:35
 * To change this template use File | Settings | File Templates.
 */
class CorrectDataController {

    def addUserInAnnotationTerm = {
         List<AnnotationTerm> list =  AnnotationTerm.findAll()
         list.each { annotationTerm ->
              if(!annotationTerm.user) {
                  Annotation annotation = annotationTerm.annotation
                  annotationTerm.user = annotation.user
                  annotationTerm.save(flush:true)
              }

         }


    }

}
