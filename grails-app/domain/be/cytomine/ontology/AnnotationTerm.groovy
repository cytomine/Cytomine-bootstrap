package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.apache.log4j.Logger

class AnnotationTerm extends CytomineDomain implements Serializable {

    UserAnnotation userAnnotation
    Term term
    SecUser user

    static constraints = {
    }

    String toString() {
        "[" + this.id + " <" + userAnnotation + "," + term + "," + user + ">]"
    }

    static AnnotationTerm link(UserAnnotation annotation, Term term,SecUser user) {
        if (!annotation) throw new WrongArgumentException("Annotation cannot be null")
        if (!term) throw new WrongArgumentException("Term cannot be null")
        if (!user) throw new WrongArgumentException("User cannot be null")
        def annotationTerm = AnnotationTerm.findWhere(userAnnotation: annotation, 'term': term,'user': user)
        if (annotationTerm) throw new AlreadyExistException("Annotation - term already exist")
        //Annotation.withTransaction {
        if (!annotationTerm) {
            annotationTerm = new AnnotationTerm(user: user)
            annotation?.addToAnnotationTerm(annotationTerm)
            term?.addToAnnotationTerm(annotationTerm)
            annotation.refresh()
            term.refresh()
            annotationTerm.save(flush: true)
        } else throw new WrongArgumentException("Annotation " + annotation.id + " and term " + term.id + " are already mapped with user " + user.id)
        //}
        return annotationTerm
    }

    static void unlink(UserAnnotation annotation, Term term,SecUser user) {

        if (!annotation) throw new WrongArgumentException("Annotation cannot be null")
        if (!term) throw new WrongArgumentException("Term cannot be null")
        if (!user) throw new WrongArgumentException("User cannot be null")
        def annotationTerm = AnnotationTerm.findWhere(userAnnotation: annotation, 'term': term, 'user': user)
        if (!annotationTerm) throw new WrongArgumentException("Annotation - term - user not exist")

        if (annotationTerm) {
            annotation?.removeFromAnnotationTerm(annotationTerm)
            term?.removeFromAnnotationTerm(annotationTerm)
            annotation.refresh()
            term.refresh()
            annotationTerm.delete(flush: true)
        }
    }

    static AnnotationTerm createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static AnnotationTerm createFromData(jsonAnnotationTerm) {
        def annotationTerm = new AnnotationTerm()
        getFromData(annotationTerm, jsonAnnotationTerm)
    }

    static AnnotationTerm getFromData(annotationTerm, jsonAnnotationTerm) {
        println jsonAnnotationTerm
        try{annotationTerm.userAnnotation = UserAnnotation.get(Long.parseLong(jsonAnnotationTerm.userannotation.toString()))}
        catch(Exception e) {
           println e
        }
        annotationTerm.term = Term.get(jsonAnnotationTerm.term.toString())
        annotationTerm.user = SecUser.get(jsonAnnotationTerm.user.toString())
        if (!annotationTerm.userAnnotation) throw new WrongArgumentException("Annotation ${jsonAnnotationTerm.userannotation.toString()} doesn't exist!")
        if (!annotationTerm.term) throw new WrongArgumentException("Term ${jsonAnnotationTerm.term.toString()} doesn't exist!")
        if (!annotationTerm.user) throw new WrongArgumentException("User ${jsonAnnotationTerm.user.toString()} doesn't exist!")
        return annotationTerm;
    }


    def getCallBack() {
        HashMap<String, Object> callback = new HashMap<String, Object>();
        callback.put("annotationID", this.userAnnotation.id)
        callback.put("termID", this.term.id)
        callback.put("imageID", this.userAnnotation.image.id)
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + AnnotationTerm.class)
        JSON.registerObjectMarshaller(AnnotationTerm) {
            def returnArray = [:]
            //returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['userannotation'] = it.userAnnotation?.id
            returnArray['term'] = it.term?.id
            returnArray['user'] = it.user?.id
            return returnArray
        }
    }

     public Project projectDomain() {
        return userAnnotation.image.project
    }
}
