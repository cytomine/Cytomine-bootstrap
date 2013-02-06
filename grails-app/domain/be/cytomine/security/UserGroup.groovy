package be.cytomine.security

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
class UserGroup {

    User user
    Group group

    static belongsTo = [user: User,group: Group]

    static mapping = {
        version false
    }

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
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static UserGroup createFromDataWithId(json) {
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
    static UserGroup createFromData(def json) {
        insertDataIntoDomain(new UserGroup(), json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static UserGroup insertDataIntoDomain(def domain, def json) {
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
            returnArray['id'] = it.hashCode()
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
