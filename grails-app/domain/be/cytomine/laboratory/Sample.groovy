package be.cytomine.laboratory

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.image.AbstractImage
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A sample is a source of image
 * This is a real thing: blood, a mouse lung,...
 */
class Sample extends CytomineDomain implements Serializable{

    String name

    static constraints = {
        name(blank: false, unique: true)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Sample insertDataIntoDomain(def json,def domain = new Sample()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json,'name')
        domain.created = JSONUtils.getJSONAttrDate(json,'created')
        domain.updated = JSONUtils.getJSONAttrDate(json,'updated')
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Sample.class)
        JSON.registerObjectMarshaller(Sample) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            return returnArray
        }
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Sample.withNewSession {
            if(name) {
                Sample sampleAlreadyExist = Sample.findByName(name)
                if(sampleAlreadyExist && (sampleAlreadyExist.id!=id))  throw new AlreadyExistException("Sample "+name + " already exist!")
            }
        }
    }
}
