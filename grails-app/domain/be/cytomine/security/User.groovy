package be.cytomine.security

import be.cytomine.processing.SoftwareProject
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A cytomine human user
 */
class User extends SecUser {

    transient springSecurityService

    String firstname
    String lastname
    String email
    String color
    String skypeAccount
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
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static User createFromData(def json) {
        def user = new User()
        try {user.id = json.id} catch (Exception e) {}
        insertDataIntoDomain(user, json)
    }
    
    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */         
    static User insertDataIntoDomain(def domain, def json) {
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
        JSON.registerObjectMarshaller(User) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['username'] = it.username
            returnArray['firstname'] = it.firstname
            returnArray['lastname'] = it.lastname
            returnArray['email'] = it.email
            returnArray['sipAccount'] = it.sipAccount
            if (it.id == it.springSecurityService.principal?.id) {
                returnArray['publicKey'] = it.publicKey
                returnArray['privateKey'] = it.privateKey
            }
            returnArray['color'] = it.color
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
            returnArray['algo'] = it.algo()
            return returnArray
        }
    }
}
