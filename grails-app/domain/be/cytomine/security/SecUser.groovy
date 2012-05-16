package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException

class SecUser extends CytomineDomain {

    String username
    String password
    String publicKey
    String privateKey
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    Boolean transactionInProgress = false //indicates whether the current user is doing several actions seen as only one action

    static transients = ["currentTransaction", "nextTransaction"]

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
        id unique: true
    }

    static mapping = {
        password column: '`password`'
        id(generator: 'assigned', unique: true)
    }

    Set<SecRole> getAuthorities() {
        SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
    }

    String realUsername() {
        return username
    }


    def generateKeys() {
        println "GENERATE KEYS"
        String privateKey = UUID.randomUUID().toString();
        String publicKey = UUID.randomUUID().toString();
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
        if (isDirty('password')) {
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
        password = springSecurityService.encodePassword(password)
    }
}
