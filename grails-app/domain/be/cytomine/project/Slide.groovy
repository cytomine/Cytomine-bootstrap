package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.image.AbstractImage
import grails.converters.JSON
import org.apache.log4j.Logger

class Slide extends CytomineDomain implements Serializable{

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

     static Slide createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static Slide createFromData(jsonSlide) {
        def image = new Slide()
        getFromData(image, jsonSlide)
    }

    static Slide getFromData(Slide slide, jsonSlide) {
        slide.created = (!jsonSlide.created.toString().equals("null")) ? new Date(Long.parseLong(jsonSlide.created)) : null
        slide.updated = (!jsonSlide.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonSlide.updated)) : null
        slide.name = jsonSlide.name
        slide.index = jsonSlide.index
        return slide;
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Slide.class)
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
