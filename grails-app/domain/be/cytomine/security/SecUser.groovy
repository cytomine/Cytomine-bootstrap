package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException

class SecUser extends CytomineDomain {

    String username
    String password
    String newPassword = null
    String publicKey
    String privateKey
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    Boolean transactionInProgress = false //indicates whether the current user is doing several actions seen as only one action

    static transients = ["newPassword", "currentTransaction", "nextTransaction"]

    //Map userGroup
    //static hasMany = [userGroup: UserGroup]
    void checkAlreadyExist() {
        SecUser.withNewSession {
            SecUser user = SecUser.findByUsername(username)
            if(user && (user.id!=id))  throw new AlreadyExistException("User "+username + " already exist!")
        }
    }

    static constraints = {
        username blank: false, unique: true
        password blank: false
        newPassword(nullable : true, black : false)
        id unique: true
    }

    static mapping = {
        password column: '`password`'
        id(generator: 'assigned', unique: true)
    }

    Set<SecRole> getAuthorities() {
        SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
    }

    boolean isAdmin() {
        if(SecUserSecRole.get(id,SecRole.findByAuthority("ROLE_ADMIN").id))
            return true
        else return false
    }

    String realUsername() {
        return username
    }


    def generateKeys() {
        log.info "GENERATE KEYS"
        String privateKey = UUID.randomUUID().toString()
        String publicKey = UUID.randomUUID().toString()
        this.setPrivateKey(privateKey)
        this.setPublicKey(publicKey)
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
            encodePassword()
        }
    }

    boolean algo() {
        return false
    }

    String toString() {
        return username
    }

    protected void encodePassword() {
        if (password == "") password = "random_password"
        password = springSecurityService.encodePassword(password)
    }
}
