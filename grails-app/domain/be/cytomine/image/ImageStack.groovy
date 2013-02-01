package be.cytomine.image

/**
 * TODOSTEVBEN: doc
 */
class ImageStack {

    static hasMany = [ imageInstances : ImageInstance]

    Long timestamp

    static constraints = {
    }
}
