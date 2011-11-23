package be.cytomine.security

import grails.converters.JSON
import org.apache.commons.lang.builder.HashCodeBuilder

class UserGroup {

    User user
    Group group

    static mapping = {
        version false
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (user) builder.append(user.id)
        if (group) builder.append(group.id)
        builder.toHashCode()
    }

    static UserGroup link(User user, Group group) {
        def userGroup = UserGroup.findByUserAndGroup(user, group)
        if (!userGroup) {
            userGroup = new UserGroup()
            user?.addToUserGroup(userGroup)
            group?.addToUserGroup(userGroup)
            userGroup.save(flush: true)
        }
    }

    static void unlink(User user, Group group) {
        def userGroup = UserGroup.findByUserAndGroup(user, group)
        if (userGroup) {
            user?.removeFromUserGroup(userGroup)
            group?.removeFromUserGroup(userGroup)
            userGroup.refresh()
            userGroup.delete(flush: true)
        } else {println "no link between " + user?.username + " " + group?.name}
    }

    static UserGroup getFromData(UserGroup userGroup, jsonUserGroup) {
        userGroup.group = Group.read(jsonUserGroup.group)
        userGroup.user = User.read(jsonUserGroup.user)
        return userGroup;
    }

    static UserGroup createFromData(data) {
        getFromData(new UserGroup(), data)
    }

      static UserGroup createFromDataWithId(json)  {
        def domain = createFromData(json)
        domain.id = json.id
        return domain
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + UserGroup.class
        JSON.registerObjectMarshaller(UserGroup) {
            def returnArray = [:]
            returnArray['id'] = it.hashCode()
            returnArray['user'] = it.user.id
            returnArray['group'] = it.group.id
            return returnArray
        }
    }
}
