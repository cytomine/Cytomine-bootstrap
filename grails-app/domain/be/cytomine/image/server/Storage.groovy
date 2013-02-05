package be.cytomine.image.server

import be.cytomine.security.SecUser

/**
 * TODOSTEVBEN: doc
 */
class Storage {

    String name
    String basePath

    SecUser owner

    //ssh config
    String ip
    String username
    String password
    String publicKey
    Integer port

    static constraints = {
        name(unique: false)
        basePath(nullable: false, blank: false)
        username(nullable: false)
        password(nullable: true)
        publicKey(nullable : true)
    }




}
