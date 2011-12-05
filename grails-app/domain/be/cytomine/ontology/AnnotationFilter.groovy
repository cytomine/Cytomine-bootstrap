package be.cytomine.ontology

import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.security.SecUser
import be.cytomine.CytomineDomain

class AnnotationFilter extends CytomineDomain implements Serializable {

    String name
    Project project

    static hasMany = [term: Term, user: User]

    static constraints = {
        name (nullable : false, blank : false)
        project (nullable: false)
    }

    static AnnotationFilter createFromDataWithId(json) throws CytomineException {
        def annotationFilter = createFromData(json)
        try {annotationFilter.id = json.id} catch (Exception e) {}
        return annotationFilter
    }

    static AnnotationFilter createFromData(jsonTerm) throws CytomineException {
        def term = new AnnotationFilter()
        getFromData(term, jsonTerm)
    }

    static AnnotationFilter getFromData(AnnotationFilter annotationFilter, json) throws CytomineException {
        annotationFilter.name = json.name
        annotationFilter.project = Project.read(json.project)
        json.users?.each { userID ->
            SecUser user = SecUser.read(userID)
            if (user) annotationFilter.addToUser(user)
        }
        json.terms?.each { termID ->
            Term term = SecUser.read(termID)
            if (term) annotationFilter.addToTerm(term)
        }
        return annotationFilter;
    }

    def getCallBack() {
        return [annotationFilterID: this?.id] //not sure...here
    }
}
