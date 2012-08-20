package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.image.AbstractImageGroup
import be.cytomine.project.Project
import grails.converters.JSON

class Group extends CytomineDomain {

    String name
    //Map userGroup
    static belongsTo = Project
    static hasMany = [abstractimagegroup: AbstractImageGroup]


    static mapping = {
        table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
    }

    static constraints = {
        name(blank: false, unique: true)
    }

    def userGroups() {
        UserGroup.findAllByGroup(this)
    }

    static Group getFromData(Group group, jsonGroup) {
        group.name = jsonGroup.name
        return group;
    }

    static Group createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static Group createFromData(data) {
        getFromData(new Group(), data)
    }

    String toString() {
        name
    }

    def abstractimages() {
        return abstractimagegroup.collect {
            it.abstractimage
        }
    }

    def users() {
        return userGroups().collect {
            it.user
        }
    }

    def projects() {
        return projectGroup.collect {
            it.project
        }
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Group.class
        JSON.registerObjectMarshaller(Group) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            return returnArray
        }
    }

}
