package be.cytomine.security

import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.restapidoc.annotation.RestApiObject

/**
 * A cytomine human user
 */
@RestApiObject(name = "user", description="A cytomine human user")
class User extends SecUser {

    transient springSecurityService

    @RestApiObjectField(description = "The firstname of the user")
    String firstname

    @RestApiObjectField(description = "The lastname of the user")
    String lastname

    @RestApiObjectField(description = "The email of the user")
    String email

    String color //deprecated

    @RestApiObjectField(description = "The skype account of the user")
    String skypeAccount

    @RestApiObjectField(description = "The SIP account of the user")
    String sipAccount

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "admin", description = "(ONLY VISIBLE WHEN DOING GET /api/user/id.format service) True if the user is ADMIN ",allowedType = "boolean",useForCreation = false),
        @RestApiObjectField(apiFieldName = "user", description = "(ONLY VISIBLE WHEN DOING GET /api/user/id.format service) True if the user is NOT ADMIN and is USER ",allowedType = "boolean",useForCreation = false),
        @RestApiObjectField(apiFieldName = "ghest", description = "(ONLY VISIBLE WHEN DOING GET /api/user/id.format service) True if the user is NOT ADMIN, NOT USER but a simple GHEST ",allowedType = "boolean",useForCreation = false)
    ])
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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = SecUser.getDataFromDomain(domain)
        returnArray['firstname'] = domain?.firstname
        returnArray['lastname'] = domain?.lastname
        returnArray['email'] = domain?.email
        returnArray['sipAccount'] = domain?.sipAccount
        if (!(domain?.springSecurityService?.principal instanceof String) && domain?.id == domain?.springSecurityService?.principal?.id) {
            returnArray['publicKey'] = domain?.publicKey
            returnArray['privateKey'] = domain?.privateKey
            returnArray['passwordExpired'] = domain?.passwordExpired
        }
        returnArray['color'] = domain?.color
        returnArray
    }


}
