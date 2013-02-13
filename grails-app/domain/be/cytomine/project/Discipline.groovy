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
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Discipline createFromData(def json) {
        def discipline = new Discipline()
        try {discipline.id = json.id} catch (Exception e) {}
        insertDataIntoDomain(discipline, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Discipline insertDataIntoDomain(def domain, def json) {
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        return domain;
    }
}
