package be.cytomine.security

import be.cytomine.project.Project
import grails.converters.JSON
import org.apache.commons.lang.builder.HashCodeBuilder

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
        println "Register custom JSON renderer for " + UserGroup.class
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
