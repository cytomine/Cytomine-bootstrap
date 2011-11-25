package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 23/05/11
 * Time: 9:16
 * To change this template use File | Settings | File Templates.
 */
class CommandHistory extends CytomineDomain {
    Command command
    String prefixAction = "" //undo, redo or nothing
    Project project

    static constraints = {
        project(nullable: true)
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + CommandHistory.class
        JSON.registerObjectMarshaller(CommandHistory) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['command'] = it.command
            returnArray['prefixAction'] = it.prefixAction

            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null

            return returnArray
        }
    }

}
