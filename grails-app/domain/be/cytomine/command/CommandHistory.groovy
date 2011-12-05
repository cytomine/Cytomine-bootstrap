package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * @author ULG-GIGA Cytomine Team
 * The CommandHistory class define a history item for a project.
 * It contains the command that was launch for a project and its method (undo/redo/nothing)
 */
class CommandHistory extends CytomineDomain {

    /**
     * Command that was launch
     */
    Command command

    /**
     * Project concerned by the command
     */
    Project project

    /**
     * Type of operation for the command (undo, redo, nothing)
     */
    String prefixAction = ""

    static constraints = {
        project(nullable: true)
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + CommandHistory.class
        JSON.registerObjectMarshaller(CommandHistory) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['class'] = it.class
            returnArray['command'] = it.command
            returnArray['prefixAction'] = it.prefixAction

            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null

            return returnArray
        }
    }

}
