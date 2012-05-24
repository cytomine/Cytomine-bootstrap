package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.json.JSONElement

/**
 * @author ULG-GIGA Cytomine Team
 * The Command class define a command which package on operation on a domain
 * It contains data about relevant domain, user who launch command,...
 */
class Command extends CytomineDomain {

    def messageSource
    def domainService
    def responseService

    /**
     * JSON string with relevant field data
     */
    String data

    /**
     * JSON object with data
     */
    JSONElement json
    static transients = ["json"]

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
     * Flag that indicate that the message will be show or not
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

    static mapping = {
        version: false
    }
    static constraints = {
        data(type: 'text', maxSize: ConfigurationHolder.config.cytomine.maxRequestSize, nullable: true)
        actionMessage(nullable: true)
        project(nullable: true)
        serviceName(nullable: true)
        transaction(nullable: true)
    }

    /**
     * Load domaine service
     */
    void initService() {
        if (!service) {
            service = grailsApplication.getMainContext().getBean(serviceName)
        }
    }

    /**
     * Add a project concerned by this command
     * @param project Project concerned by this command
     */
    void initCurrentCommantProject(Project project) {
        this.project = project;
    }

    public String toString() {
        return this.id + "[" + this.created + "]";
    }

    /**
     * Get the class name of an object without package name
     * @param o Object
     * @return Class name (without package) of o
     */
    protected String getClassName(Object o) {
        String name = o.getClass()   //be.cytomine.image.Image
        String[] array = name.split("\\.")  //[be,cytomine,image,Image]
        log.info array.length
        return array[array.length - 1] // Image
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

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Command.class
        JSON.registerObjectMarshaller(Command) {
            def returnArray = [:]

            returnArray['CLASSNAME'] = it.class
            returnArray['serviceName'] = it.serviceName
            returnArray['action'] = it.actionMessage + " by " + it?.user?.username
            returnArray['data'] = it.data
            returnArray['user'] = it.user
            returnArray['type'] = "UNKNOWN"
            if (it instanceof AddCommand) returnArray['type'] = "ADD"
            else if (it instanceof EditCommand) returnArray['type'] = "EDIT"
            else if (it instanceof DeleteCommand) returnArray['type'] = "DELETE"

            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null

            return returnArray
        }
    }
}
