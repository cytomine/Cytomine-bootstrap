package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * A discipline is a thematic for a project
 */
@ApiObject(name = "discipline")
class Discipline extends CytomineDomain implements Serializable{

    @ApiObjectFieldLight(description = "The name of the discipline")
    String name

    static constraints = {
        name(blank: false, unique: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Discipline.withNewSession {
            if(name) {
                Discipline disciplineAlreadyExist = Discipline.findByName(name)
                if(disciplineAlreadyExist && (disciplineAlreadyExist.id!=id))  {
                   throw new AlreadyExistException("Discipline "+name + " already exist!")
                }
            }
        }
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + this.class)
        JSON.registerObjectMarshaller(Discipline) { domain ->
            return getDataFromDomain(domain)
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Discipline insertDataIntoDomain(def json, def domain = new Discipline()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        return returnArray
    }

}
