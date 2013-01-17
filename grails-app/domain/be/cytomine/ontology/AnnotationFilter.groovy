package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.utils.JSONUtils

/**
 * Annotation filter define a set of filter for annotation listing
 */
class AnnotationFilter extends CytomineDomain implements Serializable {

    String name
    Project project
    User user

    static hasMany = [terms: Term, users: SecUser]

    static constraints = {
        name (nullable : false, blank : false)
        project (nullable: false)
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     * @throws CytomineException Error during domain creation
     */
    static AnnotationFilter createFromDataWithId(def json) throws CytomineException {
        def annotationFilter = createFromData(json)
        try {annotationFilter.id = json.id} catch (Exception e) {}
        return annotationFilter
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static AnnotationFilter createFromData(def json) throws CytomineException {
        def term = new AnnotationFilter()
        insertDataIntoDomain(term, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AnnotationFilter insertDataIntoDomain(def domain, def json) throws CytomineException {
        domain.name = JSONUtils.getJSONAttrStr(json,'name')
        domain.project = JSONUtils.getJSONAttrDomain(json,"project",new Project(),false)
        domain.user = JSONUtils.getJSONAttrDomain(json,"user",new User(),false)
        json.users?.each { userID ->
            SecUser user = SecUser.read(userID)
            if (user) domain.addToUsers(user)
        }
        json.terms?.each { termID ->
            Term term = Term.read(termID)
            if (term) domain.addToTerms(term)
        }
        return domain;
    }

    /**
     * Create callback metadata
     * Callback will be send whith request response when add/update/delete on this send
     * @return Callback for this domain
     */
    def getCallBack() {
        return [annotationFilterID: this?.id]
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + AnnotationFilter.class)
        JSON.registerObjectMarshaller(AnnotationFilter) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['user'] = it.user?.id
            returnArray['project'] = it.project?.id
            returnArray['terms'] = it.terms.collect { it.id }
            returnArray['users'] = it.users.collect { it.id }
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
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
