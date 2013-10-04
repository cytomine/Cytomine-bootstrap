package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.security.SecUser
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Term added to an annotation by a real user (not a job!)
 * Many user can add a term to a single annotation (not only the user that created this annotation)
 */
class AnnotationTerm extends CytomineDomain implements Serializable {

    UserAnnotation userAnnotation
    Term term
    SecUser user

    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AnnotationTerm insertDataIntoDomain(def json, def domain = new AnnotationTerm()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
        domain.userAnnotation = JSONUtils.getJSONAttrDomain(json, "userannotation", new UserAnnotation(), true)
        domain.term = JSONUtils.getJSONAttrDomain(json, "term", new Term(), true)
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)

        if(domain.term.ontology!=domain.userAnnotation.project.ontology) {
            throw new WrongArgumentException("Term ${domain.term} from ontology ${domain.term.ontology} is not in ontology from the annotation project (${domain.userAnnotation.project.ontology.id})")
        }
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
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return userAnnotation.container();
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + AnnotationTerm.class)
        JSON.registerObjectMarshaller(AnnotationTerm) {
            def returnArray = [:]
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
     * You need to override userDomainCreator() in domain class
     * @return Domain user
     */
    public SecUser userDomainCreator() {
        return user;
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        AnnotationTerm.withNewSession {
            if(userAnnotation && term && user) {
                AnnotationTerm atAlreadyExist = AnnotationTerm.findByUserAnnotationAndTermAndUser(userAnnotation,term,user)
                if (atAlreadyExist != null && (atAlreadyExist.id != id)) {
                    throw new AlreadyExistException("Annotation-Term with annotation ${userAnnotation.id} and term ${term.id} and ${user.id})")
                }
            }
        }
    }
}
