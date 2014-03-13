package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight

/**
 * Cytomine user.
 * Its the parent class for "user" (human) and "user job" (algo).
 */
//@ApiObject(name = "user")
class SecUser extends CytomineDomain implements Serializable {

    @ApiObjectFieldLight(description = "The username of the user")
    String username

    @ApiObjectFieldLight(description = "The user password", presentInResponse = false)
    String password
    String newPassword = null

    @ApiObjectFieldLight(description = "The user public key", mandatory = false, defaultValue = "A generated key")
    String publicKey

    @ApiObjectFieldLight(description = "The user private key", mandatory = false, defaultValue = "A generated key")
    String privateKey

    @ApiObjectFieldLight(description = "If true, account is enabled", useForCreation = false,presentInResponse = false)
    boolean enabled

    @ApiObjectFieldLight(description = "If true, account is expired", useForCreation = false,presentInResponse = false)
    boolean accountExpired

    @ApiObjectFieldLight(description = "If true, account is locked",useForCreation = false,presentInResponse = false)
    boolean accountLocked

    @ApiObjectFieldLight(description = "If true, password is expired",useForCreation = false,presentInResponse = false)
    boolean passwordExpired


    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "algo", description = "If true, user is a userjob",allowedType = "boolean",useForCreation = false)
    ])
    static transients = ["newPassword", "currentTransaction", "nextTransaction"]

    static constraints = {
        username blank: false, unique: true
        password blank: false
        newPassword(nullable : true, blank : false)
        publicKey (nullable : true, blank : false)
        privateKey (nullable : true, blank : false)
        id unique: true
    }

    static mapping = {
        password column: '`password`'
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['username'] = domain?.username
        returnArray['algo'] = domain?.algo()
        returnArray
    }

    def beforeInsert() {
        super.beforeInsert()
        encodePassword()
        generateKeys()
    }

    def beforeUpdate() {
        super.beforeUpdate()
        if (newPassword) {
            password = newPassword
            passwordExpired = false
            encodePassword()
        }
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        SecUser.withNewSession {
            SecUser user = SecUser.findByUsername(username)
            if(user && (user.id!=id)) {
                throw new AlreadyExistException("User "+username + " already exist!")
            }
        }
    }

    /**
     * Get user roles
     */
    Set<SecRole> getAuthorities() {
        SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
    }

    /**
     * Check if user is a cytomine admin
     * Rem: a project admin is not a cytomine admin
     */
    boolean isAdmin() {
        return (SecUserSecRole.get(id,SecRole.findByAuthority("ROLE_ADMIN").id) != null)
    }

    boolean isAdminAuth() {
        return isAdmin()
    }

    boolean isUserAuth() {
        return (SecUserSecRole.get(id,SecRole.findByAuthority("ROLE_USER").id) != null)
    }

    boolean isGuestAuth() {
        return (SecUserSecRole.get(id,SecRole.findByAuthority("ROLE_GUEST").id) != null)
    }

    /**
     * Username of the human user back to this user
     * If User => humanUsername is username
     * If Algo => humanUsername is user that launch algo username
     */
    String humanUsername() {
        return username
    }

    /**
     * Generate public/privateKey for user authentification
     */
    def generateKeys() {
        String privateKey = UUID.randomUUID().toString()
        String publicKey = UUID.randomUUID().toString()
        this.setPrivateKey(privateKey)
        this.setPublicKey(publicKey)
    }

    /**
     * Check if user is an algo (otherwise its an human)
     */
    boolean algo() {
        return false
    }

    String toString() {
        return username
    }

    protected void encodePassword() {
        log.info "encodePassword for user="+username
        if(password.size()<4) throw new WrongArgumentException("Your password must have at least 4 characters!")
        if (password == "") password = ".+7dWl_=]@8%,<&"
        password = springSecurityService.encodePassword(password)
    }

    /**
     * Return domain user (annotation user, image user...)
     * By default, a domain has no user.
     * You need to override userDomainCreator() in domain class
     * @return Domain user
     */
    public SecUser userDomainCreator() {
        return this;
    }
}
