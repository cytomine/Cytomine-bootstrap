package be.cytomine.project

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.ontology.Ontology
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.command.*

class ProjectService extends ModelService {

    static transactional = true
    def ontologyService
    def cytomineService
    def commandService
    def domainService
    def userGroupService

    boolean saveOnUndoRedoStack = false

    def list() {
        Project.list(sort: "name")
    }

    def list(Ontology ontology) {
        Project.findAllByOntology(ontology)
    }

    def list(User user) {
        user.projects()
    }

    def list(Discipline discipline) {
        project.findAllByDiscipline(discipline)
    }

    def read(def id) {
        Project.read(id)
    }

    def get(def id) {

        Project.get(id)
    }

    def lastAction(Project project, def max) {
        return CommandHistory.findAllByProject(project, [sort: "created", order: "desc", max: max])
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def json) {
        String oldName = Project.get(json.id)?.name
        User currentUser = cytomineService.getCurrentUser()
        def response = executeCommand(new EditCommand(user: currentUser), json)
        String newName = Project.get(json.id)?.name
         //Validate and save domain
        log.debug "oldName = " + oldName
        Group group = Group.findByName(oldName)
        log.info "rename group " + group?.name + "(" + group + ") by " + newName
        if (group) {
            group.name = newName
            group.save(flush: true)
        }
        return response
    }

    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(Project.createFromDataWithId(json), printMessage)
    }

    def create(Project domain, boolean printMessage) {
        //Save new object
        log.debug "create2"
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Project.get(json.id), printMessage)
    }

    def destroy(Project domain, boolean printMessage) {
        //Build response message
        //Delete all command / command history from project
        CommandHistory.findAllByProject(domain).each { it.delete() }
        Command.findAllByProject(domain).each {
            it
            UndoStackItem.findAllByCommand(it).each { it.delete()}
            RedoStackItem.findAllByCommand(it).each { it.delete()}
            it.delete()
        }
        //Delete group map with project
        Group projectGroup = Group.findByName(domain.name);

        if (projectGroup) {
            projectGroup.name = "TO REMOVE " + domain.id
            log.info "group " + projectGroup + " will be renamed"
            projectGroup.save(flush: true)
        }
        def groups = domain.groups()
//        def l = []
//        l += groups

        groups.each { group ->
            //for each group, delete user link
            def users = group.users()
            users.each { user ->
                userGroupService.unlink(user, group)
            }
            ProjectGroup.unlink(domain, group)
            //delete group
            group.delete(flush:true)
        }

        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Project(), json), printMessage)
    }

    def edit(Project domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Project createFromJSON(def json) {
        return Project.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Project project = Project.get(json.id)
        if (!project) throw new ObjectNotFoundException("Project " + json.id + " not found")
        return project
    }

}
