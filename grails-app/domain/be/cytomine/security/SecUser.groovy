package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField

/**
 * Cytomine user.
 * Its the parent class for "user" (human) and "user job" (algo).
 */
@ApiObject(name = "user")
class SecUser extends CytomineDomain implements Serializable {

    @ApiObjectField(description = "The username of the user")
    String username

    String password
    String newPassword = null
    String publicKey
    String privateKey

    @ApiObjectField(
            description = "If true, account is enabled",
            allowedType = "boolean")
    boolean enabled
    @ApiObjectField(
            description = "If true, account is expired",
            allowedType = "boolean")

    boolean accountExpired

    @ApiObjectField(
            description = "If true, account is locked",
            allowedType = "boolean")
    boolean accountLocked

    @ApiObjectField(
            description = "If true, password is expired",
            allowedType = "boolean")
    boolean passwordExpired

    static transients = ["newPassword", "currentTransaction", "nextTransaction"]

    static constraints = {
        username blank: false, unique: true
        password blank: false
        newPassword(nullable : true, blank : false)
        id unique: true
    }

    static mapping = {
        password column: '`password`'
        id(generator: 'assigned', unique: true)
        sort "id"
    }


    def beforeInsert() {
        println "SecUser.beforeValidate"
        super.beforeInsert()
        encodePassword()
        generateKeys()
    }

    def beforeUpdate() {
        println "SecUser.beforeUpdate"
        super.beforeUpdate()
        if (newPassword) {
            password = newPassword
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
        println "encodePassword for user="+username
        if(password.size()<4) throw new WrongArgumentException("Your password must have at least 5 characters!")
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
