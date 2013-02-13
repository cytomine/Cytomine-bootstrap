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

    static mapping = {
        table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
    }

    static constraints = {
        name(blank: false, unique: true)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Group insertDataIntoDomain(def json,def domain=new Group()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json,'name',true)
        return domain;
    }

    String toString() {
        name
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
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
