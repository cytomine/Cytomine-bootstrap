package be.cytomine.image.server

/**
 * TODOSTEVBEN: doc
 */
class Storage {

    String name
    String basePath
    String serviceUrl
    String ip
    String username
    String password
    Integer port

    static constraints = {
        name(maxSize: 8, unique: true) //ais storage max length
        basePath(nullable: false, blank: false)
        serviceUrl(nullable: false, blank: false)
    }

    String toString() {
        name + "(" + serviceUrl + " : " + basePath + ")"
    }


}
