package be.cytomine.image

/**
 * Any secondary file related to an abstract image.
 * Useful for image formats like VMS/MRXS which are composed of many files
 * Useful in case you convert the image in an another format and want to store the original file
 */
class NestedFile {

    String originalFilename
    String filename
    byte[] data
    Long size

    static belongsTo = [abstractImage : AbstractImage]

    static constraints = {
        originalFilename(blank: false)
        filename(blank: false)
        data(nullable : true)
        size(nullable : true)
    }
}
