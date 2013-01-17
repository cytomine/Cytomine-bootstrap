package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.utils.JSONUtils
import be.cytomine.security.UserJob

/**
 * Term added to an annotation by a real user (not a job!)
 * Many user can add a term to a single annotation (not only the user that created this annotation)
 */
class AnnotationTerm extends CytomineDomain implements Serializable {

    UserAnnotation userAnnotation
    Term term
    SecUser user

    static constraints = {
    }

    String toString() {
        "[" + this.id + " <" + userAnnotation + "," + term + "," + user + ">]"
    }

    /**
     * Add a term to an annotation
     */
    static AnnotationTerm link(UserAnnotation annotation, Term term,SecUser user) {

        if (!annotation) {
            throw new WrongArgumentException("Annotation cannot be null")
        }
        if (!term) {
            throw new WrongArgumentException("Term cannot be null")
        }
        if (!user) {
            throw new WrongArgumentException("User cannot be null")
        }

        def annotationTerm = AnnotationTerm.findWhere(userAnnotation: annotation, 'term': term,'user': user)

        if (!annotationTerm) {
            annotationTerm = new AnnotationTerm(user: user)
            annotation?.addToAnnotationTerm(annotationTerm)
            term?.addToAnnotationTerm(annotationTerm)
            annotation.refresh()
            term.refresh()
            annotationTerm.save(flush: true)
        } else {
            throw new AlreadyExistException("Annotation " + annotation.id + " and term " + term.id + " are already mapped with user " + user.id)
        }

        return annotationTerm
    }

    /**
     * Remove a term from an annotation
     */
    static void unlink(UserAnnotation annotation, Term term,SecUser user) {

        if (!annotation) {
            throw new WrongArgumentException("Annotation cannot be null")
        }
        if (!term) {
            throw new WrongArgumentException("Term cannot be null")
        }
        if (!user) {
            throw new WrongArgumentException("User cannot be null")
        }

        def annotationTerm = AnnotationTerm.findWhere(userAnnotation: annotation, 'term': term, 'user': user)

        if (annotationTerm) {
            annotation?.removeFromAnnotationTerm(annotationTerm)
            term?.removeFromAnnotationTerm(annotationTerm)
            annotation.refresh()
            term.refresh()
            annotationTerm.delete(flush: true)
        } else {
            throw new WrongArgumentException("Annotation - term - user not exist")
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
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
        domain.userAnnotation = JSONUtils.getJSONAttrDomain(json, "userannotation", new UserAnnotation(), true)
        domain.term = JSONUtils.getJSONAttrDomain(json, "term", new Term(), true)
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
        return domain;
    }

    /**
     * Create callback metadata
     * Callback will be send whith request response when add/update/delete on this send
     * @return Callback for this domain
     */
    def getCallBack() {
        return [annotationID: this.userAnnotation.id,termID : this.term.id,imageID : this.userAnnotation.image.id]
    }

    /**
     * Get the project link with this domain type
     * @return Project of this domain
     */
     public Project projectDomain() {
        return userAnnotation.image.project
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

    /**
     * Return domain user (annotation user, image user...)
     * By default, a domain has no user.
     * You need to override userDomain() in domain class
     * @return Domain user
     */
    public SecUser userDomain() {
        return user;
    }
}
