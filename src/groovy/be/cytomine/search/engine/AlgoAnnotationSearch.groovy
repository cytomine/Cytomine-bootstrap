package be.cytomine.search.engine

import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.UserAnnotation

/**
 * Created by lrollus on 7/22/14.
 */
class AlgoAnnotationSearch extends AnnotationSearch {

    public String getClassName() {
        return AlgoAnnotation.class.name
    }

    public String getTable() {
        return "algo_annotation"
    }

    public String getTermTable() {
        return "algo_annotation_term"
    }

    public String getLinkTerm() {
        return "AND annotation.id = at.annotation_ident\n" +
                "AND term.id = at.term_id"
    }

}
