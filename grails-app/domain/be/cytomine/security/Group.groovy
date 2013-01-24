package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.image.AbstractImageGroup
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A group is a set of user
 */
class Group extends CytomineDomain {

    String name

    static hasMany = [abstractimagegroup: AbstractImageGroup]

    static mapping = {
        table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
    }

    static constraints = {
        name(blank: false, unique: true)
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Group createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Group createFromData(def json) {
        insertDataIntoDomain(new Group(), json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Group insertDataIntoDomain(def domain, def json) {
        domain.name = JSONUtils.getJSONAttrStr(json,'name',true)
        return domain;
    }

    String toString() {
        name
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Group.class)
        JSON.registerObjectMarshaller(Group) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            return returnArray
        }
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Group.withNewSession {
            Group groupAlreadyExist = Group.findByName(name)
            if(groupAlreadyExist && (groupAlreadyExist.id!=id))  throw new AlreadyExistException("Group $name already exist!")
        }
    }

}
