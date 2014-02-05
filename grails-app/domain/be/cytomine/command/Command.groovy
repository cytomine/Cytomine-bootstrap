package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONElement

/**
 * @author ULG-GIGA Cytomine Team
 * The Command class define a command which package on operation on a domain
 * It contains data about relevant domain, user who launch command,...
 */
class Command extends CytomineDomain {

    def modelService
    def responseService

    /**
     * JSON string with relevant field data
     */
    String data

    /**
     * JSON object with data
     */
    JSONElement json



    def domain

    static transients = ["json","domain"]

    /**
     * User who launch command
     */
    SecUser user
    Transaction transaction

    /**
     * Project concerned by command
     */
    Project project

    /**
     * Flag that indicate that the message will be show or not for undo/redo
     */
    boolean printMessage = true

    /**
     * Message explaining the command
     */
    String actionMessage

    /**
     * Set to false if command is not undo(redo)-able
     * By default, don't save command on stack
     */
    boolean saveOnUndoRedoStack = false

    /**
     * Service of the relevant domain for the command
     */
    def service

    /**
     * Service name of the relevant domain for the command
     */
    String serviceName

    /**
     * If command is save on undo stack, refuse undo
     * Usefull for project delete (cannot undo)
     */
    Boolean refuseUndo = false

    static constraints = {
        data(type: 'text', nullable: true)
        actionMessage(nullable: true)
        project(nullable: true)
        serviceName(nullable: true)
        transaction(nullable: true)
    }

    static mapping = {
        sort "id"
    }

    /**
     * Load domaine service thanks to its name
     */
    void initService() {
        if (!service) {
            service = grailsApplication.getMainContext().getBean(serviceName)
        }
    }

    public String toString() {
        return this.id + "[" + this.created + "]";
    }

    /**
     * Add command info for the new domain concerned by the command
     * @param newObject New domain
     * @param message Message build for the command
     */
    protected void fillCommandInfo(def newObject, String message) {
        data = newObject.encodeAsJSON()
        actionMessage = message
    }

    /**
     * Add command info for the new domain concerned by the command
     * @param newObject New json domain
     * @param message Message build for the command
     */
    protected void fillCommandInfoJSON(def newObject, String message) {
        data = newObject
        actionMessage = message
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['CLASSNAME'] = domain?.class
        returnArray['serviceName'] = domain?.serviceName
        returnArray['action'] = domain?.actionMessage + " by " + domain?.user?.username
        returnArray['data'] = domain?.data
        returnArray['user'] = domain?.user?.id
        returnArray['type'] = "UNKNOWN"
        if (domain instanceof AddCommand) returnArray['type'] = "ADD"
        else if (domain instanceof EditCommand) returnArray['type'] = "EDIT"
        else if (domain instanceof DeleteCommand) returnArray['type'] = "DELETE"

        return returnArray
    }
}
