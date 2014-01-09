package be.cytomine.security

import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField

/**
 * A cytomine human user
 */
@ApiObject(name = "user")
class User extends SecUser {

    transient springSecurityService

    @ApiObjectField(description = "The firstname of the user")
    String firstname
    @ApiObjectField(description = "The lastname of the user")
    String lastname
    @ApiObjectField(description = "The email of the user")
    String email
    String color //deprecated
    @ApiObjectField(description = "The skype account of the user")
    String skypeAccount
    @ApiObjectField(description = "The SIP account of the user")
    String sipAccount

    static constraints = {
        firstname blank: false
        lastname blank: false
        skypeAccount(nullable: true, blank:false)
        sipAccount(nullable: true, blank:false)
        email(blank: false, email: true)
        color(blank: false, nullable: true)
    }

    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }

    /**
     * Username of the human user back to this user
     * If User => humanUsername is username
     * If Algo => humanUsername is user that launch algo username
     */
    String humanUsername() {
        return username
    }

    String toString() {
        firstname + " " + lastname
    }

    /**
     * Check if user is a job
     */
    boolean algo() {
        return false
    }
    
    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */         
    static User insertDataIntoDomain(def json, def domain = new User()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.username = JSONUtils.getJSONAttrStr(json,'username')
        domain.firstname = JSONUtils.getJSONAttrStr(json,'firstname')
        domain.lastname = JSONUtils.getJSONAttrStr(json,'lastname')
        domain.email = JSONUtils.getJSONAttrStr(json,'email')
        domain.color = JSONUtils.getJSONAttrStr(json,'color')
        domain.skypeAccount = JSONUtils.getJSONAttrStr(json,'skypeAccount')
        domain.sipAccount = JSONUtils.getJSONAttrStr(json,'sipAccount')
        if (json.password && domain.password != null) {
            domain.newPassword = JSONUtils.getJSONAttrStr(json,'password') //user is updated
        } else if (json.password) {
            domain.password = JSONUtils.getJSONAttrStr(json,'password') //user is created
        }
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
        domain.enabled = true

        if (domain.getPublicKey() == null || domain.getPrivateKey() == null || json.publicKey == "" || json.privateKey == "") {
            domain.generateKeys()
        }
        return domain;
    }    

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + User.class)
        JSON.registerObjectMarshaller(User) { domain ->
            return getDataFromDomain(domain, getMappingFromAnnotation(User))
        }
    }

    static def getDataFromDomain(def domain, LinkedHashMap<String, Object> mapFields = null) {

        println "get user json"
        /* base fields + api fields */
        def json = getAPIBaseFields(domain) + getAPIDomainFields(domain, mapFields)

        /* supplementary fields : which are NOT used in insertDataIntoDomain !
        * Typically, these fields are shortcuts or supplementary information
        * from other domains
        * ::to do : hide these fields if not GUI ?
        * */
        if (!(domain.springSecurityService.principal instanceof String) && domain.id == domain.springSecurityService.principal?.id) {
            json['publicKey'] = domain.publicKey
            json['privateKey'] = domain.privateKey
        }

        json['algo'] = domain.algo()

        return json
    }
}
