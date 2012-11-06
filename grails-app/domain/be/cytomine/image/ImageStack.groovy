package be.cytomine.image

class ImageStack {

    static hasMany = [ images : ImageInstance]

    Long timestamp

    static constraints = {
    }
}
