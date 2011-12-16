package be.cytomine.security

import be.cytomine.CytomineDomain

class SecUser extends CytomineDomain {

    String username
    String password
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    Boolean transactionInProgress = false //indicates whether the current user is doing several actions seen as only one action

    static transients = ["currentTransaction", "nextTransaction"]

    //Map userGroup
    //static hasMany = [userGroup: UserGroup]

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

	def beforeInsert() {
        super.beforeInsert()
	}

	def beforeUpdate() {
        super.beforeUpdate()
	}
    
    String toString() {
        return username
    }
}
