package be.cytomine.image.server

class Storage {

    String name
    String basePath
    String serviceUrl

    static hasMany = [storageAbstractImages : StorageAbstractImage]

    static constraints = {
        name (maxSize : 8, unique : true) //ais storage max length
        basePath (nullable: false, blank : false)
        serviceUrl (nullable: false, blank : false)
    }

    def toString() {
        name + "("  + serviceUrl + " : " + basePath + ")"
    }


}
