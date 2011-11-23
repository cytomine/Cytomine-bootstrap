package be.cytomine.security

import be.cytomine.SequenceDomain
import be.cytomine.image.AbstractImageGroup
import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup
import grails.converters.JSON

class Group extends SequenceDomain {

    String name

    static belongsTo = Project
    static hasMany = [userGroup: UserGroup, projectGroup: ProjectGroup, abstractimagegroup: AbstractImageGroup]


    static mapping = {
        table "`group`" //otherwise there is a conflict with the word "GROUP" from the SQL SYNTAX
    }

    static constraints = {
        name(blank: false, unique: true)
    }

    static Group getFromData(Group group, jsonGroup) {
        group.name = jsonGroup.name
        return group;
    }

      static Group createFromDataWithId(json)  {
        def domain = createFromData(json)
        domain.id = json.id
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
        return userGroup.collect {
            it.user
        }
    }

    def projects() {
        return projectGroup.collect {
            it.project
        }
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Group.class
        JSON.registerObjectMarshaller(Group) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            return returnArray
        }
    }

}
