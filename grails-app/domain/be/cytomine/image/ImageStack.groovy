package be.cytomine.image

/**
 * TODOSTEVBEN: doc
 */
class ImageStack {

    static hasMany = [ images : ImageInstance]

    Long timestamp

    static constraints = {
    }
}
