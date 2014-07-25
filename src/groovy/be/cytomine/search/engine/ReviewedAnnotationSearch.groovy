package be.cytomine.search.engine

import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation

/**
 * Created by lrollus on 7/22/14.
 */
class ReviewedAnnotationSearch extends AnnotationSearch {

    public String getClassName() {
        return ReviewedAnnotation.class.name
    }

    public String getTable() {
        return "reviewed_annotation"
    }


    public String getTermTable() {
        return "reviewed_annotation_term"
    }

    public String getLinkTerm() {
        return "AND annotation.id = at.reviewed_annotation_terms_id\n" +
                "AND term.id = at.term_id"
    }


}
