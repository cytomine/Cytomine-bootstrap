package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * The CommandHistory class define a history item for a project.
 * It contains the command that was launch for a project and its method (undo/redo/nothing)
 * @author ULG-GIGA Cytomine Team
 */
class CommandHistory extends CytomineDomain {

    def messageService

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

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + CommandHistory.class)
        JSON.registerObjectMarshaller(CommandHistory) {
            def returnArray = [:]
            println "*****************"
            println it
            println it.properties

            it.properties.each {
                println it.key + "=" + it.value
            }

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
