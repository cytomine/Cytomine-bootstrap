package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.SecUser
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

    //redondance with command.user (perf)
    SecUser user

    //redondance with command.message (perf)
    String message

    static constraints = {
        project(nullable: true)
        prefixAction(nullable:false)
    }

    static mapping = {
        id generator: "assigned"
        command fetch: 'join'
        sort "id"
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['command'] = domain?.command
        returnArray['prefixAction'] = domain?.prefixAction
        returnArray['user'] = domain?.user
        return returnArray
    }

}
