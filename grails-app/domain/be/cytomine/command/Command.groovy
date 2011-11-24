package be.cytomine.command

import be.cytomine.SequenceDomain
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement

class Command extends SequenceDomain {

    def messageSource
    def domainService
    def responseService

    def abstractImageService
    def abstractImageGroupService
    def annotationService
    def annotationTermService
    def disciplineService
    def groupService
    def imageInstanceService
    def ontologyService
    def projectService
    def relationTermService
    def secUserSecRoleService
    def suggestedTermService
    def termService
    def userService
    def userGroupService

    String data
    String postData
    JSONElement json
    static transients = ["json"]

    User user
    Project project

    boolean printMessage = true

    static Integer MAXSIZEREQUEST = 102400

    String actionMessage

    boolean saveOnUndoRedoStack = false //by default, don't save command on stack

    static mapping = {
        version: false
    }
    static constraints = {
        data(type: 'text', maxSize: Command.MAXSIZEREQUEST, nullable: true)
        postData(type: 'text', maxSize: Command.MAXSIZEREQUEST)
        actionMessage(nullable: true)
        project(nullable: true)
    }

    void initCurrentCommantProject(Project project) { //setCur... doesn't work with spring
        println "setCurrentProject=" + project
        this.project = project;
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Command.class
        JSON.registerObjectMarshaller(Command) {
            def returnArray = [:]

            returnArray['CLASSNAME'] = it.class
            returnArray['action'] = it.getActionMessage() + " by " + it?.user?.username
            returnArray['data'] = it.data
            returnArray['user'] = it?.userId
            returnArray['type'] = "UNKNOWN"
            if (it instanceof AddCommand) returnArray['type'] = "ADD"
            else if (it instanceof EditCommand) returnArray['type'] = "EDIT"
            else if (it instanceof DeleteCommand) returnArray['type'] = "DELETE"

            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null

            return returnArray
        }
    }

    String getActionMessage() {
        return actionMessage
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
        log.info("getClassName=" + o.getClass());
        String name = o.getClass()   //be.cytomine.image.Image
        String[] array = name.split("\\.")  //[be,cytomine,image,Image]
        log.info array.length
        return array[array.length - 1] // Image
    }

    protected void fillCommandInfo(def newObject,String message) {
        data = newObject.encodeAsJSON()
        actionMessage = message
    }

}
