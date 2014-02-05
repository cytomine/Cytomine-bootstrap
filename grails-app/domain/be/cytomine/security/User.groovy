package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.SecurityACL
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField

/**
 * A cytomine human user
 */
@ApiObject(name = "user", description="A cytomine human user")
class User extends SecUser {

    transient springSecurityService

    @ApiObjectFieldLight(description = "The firstname of the user")
    String firstname

    @ApiObjectFieldLight(description = "The lastname of the user")
    String lastname

    @ApiObjectFieldLight(description = "The email of the user")
    String email

    String color //deprecated

    @ApiObjectFieldLight(description = "The skype account of the user")
    String skypeAccount

    @ApiObjectFieldLight(description = "The SIP account of the user")
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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = SecUser.getDataFromDomain(domain)
        returnArray['firstname'] = domain?.firstname
        returnArray['lastname'] = domain?.lastname
        returnArray['email'] = domain?.email
        returnArray['sipAccount'] = domain?.sipAccount
        if (!(domain?.springSecurityService.principal instanceof String) && domain?.id == domain?.springSecurityService.principal?.id) {
            returnArray['publicKey'] = domain?.publicKey
            returnArray['privateKey'] = domain?.privateKey
        }
        returnArray['color'] = domain?.color
        returnArray
    }


}
