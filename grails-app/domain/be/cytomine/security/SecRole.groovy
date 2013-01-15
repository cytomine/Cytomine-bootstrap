package be.cytomine.security

/**
 * User role (user, admin,...)
 */
class SecRole {

    String authority

    static mapping = {
        cache true
    }

    static constraints = {
        authority blank: false, unique: true
    }
}
