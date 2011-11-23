package be.cytomine.project

import be.cytomine.SequenceDomain
import be.cytomine.image.AbstractImage
import grails.converters.JSON

class Slide extends SequenceDomain {

    String name
    int index

    /*static mapping = {
        columns {
            order column:"`order`"  //otherwise there is a conflict with the word "ORDER" from the SQL SYNTAX
        }
    }*/
    static constraints = {
        name(blank: false, unique: true)
    }

    String toString() {
        name
    }

    static hasMany = [image: AbstractImage]


    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Slide.class
        JSON.registerObjectMarshaller(Slide) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            def images = []

            it.getImages().each {img ->
                def imageinfo = [:]
                imageinfo.id = img.id
                imageinfo.filename = img.filename
                imageinfo.thumb = img.getThumbURL()
                imageinfo.info = it.name
                images << imageinfo
            }
            returnArray['images'] = images
            return returnArray
        }
    }

    def getImages() {
        AbstractImage.findAllBySlide(this);
    }
}
