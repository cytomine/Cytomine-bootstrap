package be.cytomine.image

class AssociatedImage {

    String label
    byte[] imageData
    AbstractImage abstractImage

    static belongsTo = [AbstractImage]

    static constraints = {
        label(blank : false)
        imageData(nullable : false)
    }
}
