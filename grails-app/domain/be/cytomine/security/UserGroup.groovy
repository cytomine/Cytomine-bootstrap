package be.cytomine.security

import be.cytomine.Exception.AlreadyExistException
import be.cytomine.project.Project
import grails.converters.JSON
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.log4j.Logger

class UserGroup {

    def domaineService

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

	void checkAlreadyExist() {
        UserGroup.withNewSession {
            UserGroup userGroupAlreadyExist = UserGroup.findByUserAndGroup(user, group)
            if(userGroupAlreadyExist)  throw new AlreadyExistException("UserGroup "+userGroupAlreadyExist?.user + ","+ userGroupAlreadyExist?.group + " already exist!")
        }
    }

    static UserGroup getFromData(UserGroup userGroup, jsonUserGroup) {
        userGroup.group = Group.read(jsonUserGroup.group)
        userGroup.user = User.read(jsonUserGroup.user)
        return userGroup;
    }

    static UserGroup createFromData(data) {
        getFromData(new UserGroup(), data)
    }

    static UserGroup createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static void registerMarshaller(String cytomineBaseUrl) {
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
