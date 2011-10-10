package be.cytomine.processing

class ImageFilter {

    String name
    String baseUrl

    static hasMany = [ imageFilterProjects: ImageFilterProject ]

    static constraints = {
        name (blank : false, nullable : false)
        baseUrl (blank : false, nullable : false)
    }
}
