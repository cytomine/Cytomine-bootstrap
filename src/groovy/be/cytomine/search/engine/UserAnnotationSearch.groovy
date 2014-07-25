package be.cytomine.search.engine

import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project

/**
 * Created by lrollus on 7/22/14.
 */
class UserAnnotationSearch extends AnnotationSearch {

    public String getClassName() {
        return UserAnnotation.class.name
    }

    public String getTable() {
        return "user_annotation"
    }


    public String getTermTable() {
        return "annotation_term"
    }

    public String getLinkTerm() {
        return "AND annotation.id = at.user_annotation_id\n" +
                "AND term.id = at.term_id"
    }


}
