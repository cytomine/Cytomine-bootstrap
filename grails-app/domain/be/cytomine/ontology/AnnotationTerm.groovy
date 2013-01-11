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

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static AnnotationTerm createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static AnnotationTerm createFromData(def json) {
        def annotationTerm = new AnnotationTerm()
        insertDataIntoDomain(annotationTerm, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AnnotationTerm insertDataIntoDomain(def domain, def json) {
        println json
        try{domain.userAnnotation = UserAnnotation.get(Long.parseLong(json.userannotation.toString()))}
        catch(Exception e) {
           println e
        }
        domain.term = Term.get(json.term.toString())
        domain.user = SecUser.get(json.user.toString())
        if (!domain.userAnnotation) throw new WrongArgumentException("Annotation ${json.userannotation.toString()} doesn't exist!")
        if (!domain.term) throw new WrongArgumentException("Term ${json.term.toString()} doesn't exist!")
        if (!domain.user) throw new WrongArgumentException("User ${json.user.toString()} doesn't exist!")
        return domain;
    }

    def getCallBack() {
        return [
                annotationID: this.userAnnotation.id,
                termID : this.term.id,
                imageID : this.userAnnotation.image.id]
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
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
