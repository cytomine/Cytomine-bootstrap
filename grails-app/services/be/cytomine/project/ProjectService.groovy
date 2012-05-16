package be.cytomine.project

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.access.prepost.PostFilter

import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.test.Infos
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.transaction.annotation.Transactional
import be.cytomine.command.*

class ProjectService extends ModelService {

    static transactional = true
    def ontologyService
    def cytomineService
    def commandService
    def domainService
    def userGroupService
    def aclPermissionFactory
    def aclService
    def aclUtilService
    def springSecurityService


    boolean saveOnUndoRedoStack = false

    void addPermission(Project project, String username, int permission) {
        addPermission(project, username, aclPermissionFactory.buildFromMask(permission))
    }

    @PreAuthorize("#project.hasPermission('ADMIN') or hasRole('ROLE_ADMIN')")
    void addPermission(Project project, String username, Permission permission) {
        log.info "Add Permission " +  permission.toString() + " for " + username + " to " + project?.name
        aclUtilService.addPermission(project, username, permission)
    }

    @Transactional
    @PreAuthorize("#project.hasPermission('ADMIN') or #user.id == principal.id or hasRole('ROLE_ADMIN')")
    void deletePermission(Project project, SecUser user, Permission permission) {
        def acl = aclUtilService.readAcl(project)

        // Remove all permissions associated with this particular recipient
        acl.entries.eachWithIndex { entry, i ->
            log.debug "entry.permission.equals(permission)="+entry.permission.equals(permission) + " entry.sid="+entry.sid.getPrincipal()
            if (entry.sid.getPrincipal().equals(user.username) && entry.permission.equals(permission)) {
                log.debug "REMOVE PERMISSION FOR"
                acl.deleteAce(i)
            }
        }

        aclService.updateAcl(acl)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("filterObject.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list() {
        Project.list(sort: "name")
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("filterObject.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Ontology ontology) {
        Project.findAllByOntology(ontology)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("filterObject.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(User user) {
        user.projects()
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("filterObject.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Discipline discipline) {
        Project.findAllByDiscipline(discipline)
    }

    @PreAuthorize("#cytomineDomain.hasPermission(#id, 'be.cytomine.project.Project','READ') or hasRole('ROLE_ADMIN')")
    def read(def id, def cytomineDomain) {
        Project.read(id)
    }

    @PreAuthorize("#cytomineDomain.hasPermission(#id, 'be.cytomine.project.Project','READ') or hasRole('ROLE_ADMIN')")
    def get(def id, def cytomineDomain) {
        Project.get(id)
    }

    def list(Software software) {
        software.softwareProjects.collect {it.project}
    }

    def lastAction(Project project, def max) {
        return CommandHistory.findAllByProject(project, [sort: "created", order: "desc", max: max])
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    @PreAuthorize("#domain.hasPermission('WRITE') or hasRole('ROLE_ADMIN')")
    def update(def domain, def json) {
        String oldName = Project.get(json.id)?.name
        SecUser currentUser = cytomineService.getCurrentUser()
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

    @PreAuthorize("#domain.hasPermission('DELETE') or hasRole('ROLE_ADMIN')")
    def delete(def domain, def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
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
        domainService.saveDomain(domain)
        log.info("Add permission on " + domain + " to " + springSecurityService.authentication.name)
        addPermission(domain, springSecurityService.authentication.name,BasePermission.ADMINISTRATION)
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
        println "destroy project"
        //Delete all command / command history from project
        CommandHistory.findAllByProject(domain).each { it.delete() }
        Command.findAllByProject(domain).each {
            it
            UndoStackItem.findAllByCommand(it).each { it.delete()}
            RedoStackItem.findAllByCommand(it).each { it.delete()}
            it.delete()
        }
        log.info "command deleted"
        //Delete group map with project
        Group projectGroup = Group.findByName(domain.name);
        log.info "projectGroup " + projectGroup
        if (projectGroup) {
            projectGroup.name = "TO REMOVE " + domain.id
            log.info "group " + projectGroup + " will be renamed"
            projectGroup.save(flush: true)
        }
        def groups = domain.groups()
//        def l = []
        //        l += groups
        log.info "groups="+groups
        groups.each { group ->
            //for each group, delete user link
            def users = group.users()
            log.info "users="+users
            users.each { user ->
                userGroupService.unlink(user, group)
            }
            ProjectGroup.unlink(domain, group)
            //delete group
            group.delete(flush:true)
        }
        log.info "createResponseMessage"
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        log.info "deleteDomain"
        domainService.deleteDomain(domain)
        log.info "response"
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

    def edit(Project project, boolean printMessage) {
        log.debug "EDIT domain " + project.id
        Infos.printRight(project)
        log.debug "CURRENT USER " + cytomineService.getCurrentUser().username
        //Build response message
        def response = responseService.createResponseMessage(project, [project.id, project.name], printMessage, "Edit", project.getCallBack())
        //Save update
        domainService.saveDomain(project)
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
