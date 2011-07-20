package be.cytomine.image.server

import be.cytomine.image.AbstractImage

class ImageProperty {

    String key
    String value
    AbstractImage image

    static constraints = {
        key (nullable: false, empty :false)
        value (nullable: false, empty :false)
        image (nullable :false)
    }
}
