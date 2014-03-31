package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.restapidoc.annotation.RestApiObject

/**
 * Annotation filter define a set of filter for annotation listing
 */
@RestApiObject(name = "annotation filter", description="Define a set of filter for annotation listing")
class AnnotationFilter extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The filter name")
    String name

    @RestApiObjectField(description = "The project of the filter")
    Project project

    @RestApiObjectField(description = "The user that create the filter (auto field)", useForCreation = false)
    User user

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "terms", description = "Terms filter id",allowedType = "list",useForCreation = true, mandatory = false),
        @RestApiObjectField(apiFieldName = "users", description = "Users filter id",allowedType = "list",useForCreation = true, mandatory = false)
    ])
    static hasMany = [terms: Term, users: SecUser]


    static constraints = {
        name (nullable : false, blank : false)
        project (nullable: false)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AnnotationFilter insertDataIntoDomain(def json, def domain = new AnnotationFilter()) throws CytomineException {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json,'name')
        domain.project = JSONUtils.getJSONAttrDomain(json,"project",new Project(),true)
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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['user'] = domain?.user?.id
        returnArray['project'] = domain?.project?.id
        returnArray['terms'] = domain?.terms?.collect { it.id }
        returnArray['users'] = domain?.users?.collect { it.id }
        return returnArray
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
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return project.container();
    }
}
