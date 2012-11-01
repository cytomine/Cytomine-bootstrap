package be.cytomine.laboratory

import be.cytomine.CytomineDomain
import be.cytomine.image.AbstractImage
import grails.converters.JSON
import org.apache.log4j.Logger

class Sample extends CytomineDomain implements Serializable{

    String name

    static constraints = {
        name(blank: false, unique: true)
    }

    static hasMany = [image: AbstractImage]

     static Sample createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static Sample createFromData(jsonSample) {
        def image = new Sample()
        getFromData(image, jsonSample)
    }

    static Sample getFromData(Sample sample, jsonSample) {
        sample.created = (!jsonSample.created.toString().equals("null")) ? new Date(Long.parseLong(jsonSample.created)) : null
        sample.updated = (!jsonSample.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonSample.updated)) : null
        sample.name = jsonSample.name
        return sample;
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Sample.class)
        JSON.registerObjectMarshaller(Sample) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            return returnArray
        }
    }
}
