package be.cytomine.image

class NestedFile {

    String originalFilename
    String filename

    static belongsTo = [abstractImage : AbstractImage]

    static constraints = {
    }
}
