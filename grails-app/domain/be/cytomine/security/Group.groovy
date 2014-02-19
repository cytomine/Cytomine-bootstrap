package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.utils.JSONUtils
import jsondoc.annotation.ApiObjectFieldLight
import org.jsondoc.core.annotation.ApiObject

/**
 * A group is a set of user
 */
@ApiObject(name = "group", description="A group is a set of users. A user may be in many groups")
class Group extends CytomineDomain {

    @ApiObjectFieldLight(description="The group name")
    String name

    static mapping = {
        table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
        sort "id"
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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray
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
