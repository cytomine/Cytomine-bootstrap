package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.log4j.Logger

/**
 * A group is a set of user
 * UserGroup is the link between a group and a user in database
 */
class UserGroup extends CytomineDomain {

    User user
    Group group

    static belongsTo = [user: User,group: Group]

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (user) builder.append(user.id)
        if (group) builder.append(group.id)
        builder.toHashCode()
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
	void checkAlreadyExist() {
        UserGroup.withNewSession {
            UserGroup userGroupAlreadyExist = UserGroup.findByUserAndGroup(user, group)
            if(userGroupAlreadyExist)  {
                throw new AlreadyExistException("UserGroup "+userGroupAlreadyExist?.user + ","+ userGroupAlreadyExist?.group + " already exist!")
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static UserGroup insertDataIntoDomain(def json, def domain = new UserGroup()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.group = JSONUtils.getJSONAttrDomain(json, "group", new Group(), true)
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
        return domain
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + UserGroup.class)
        JSON.registerObjectMarshaller(UserGroup) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['user'] = it.user.id
            returnArray['group'] = it.group.id
            return returnArray
        }
    }

    public Project projectDomain() {
        return null;
    }

    def getCallBack() {
        return null
    }
}
