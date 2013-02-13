package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A discipline is a thematic for a project
 */
class Discipline extends CytomineDomain implements Serializable{

    String name

    static constraints = {
        name(blank: false, unique: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Discipline.class)
        JSON.registerObjectMarshaller(Discipline) {
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
}
