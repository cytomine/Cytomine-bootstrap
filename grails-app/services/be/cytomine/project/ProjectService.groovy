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
import org.apache.commons.collections.CollectionUtils
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.UploadedFile

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
        checkRetrievalConsistency(json)
        def response = executeCommand(new AddCommand(user: currentUser), json)

        //add or delete RetrievalProject
        Project project = response.object
        log.info "project="+project

        if(!json.retrievalProjects.toString().equals("null")) {
            project.refresh()
            log.info "json.retrievalProjects="+json.retrievalProjects
            json.retrievalProjects.each { idProject ->
                println "idProject="+idProject
                Long proj = Long.parseLong(idProject.toString())
                println "proj="+proj
                Project projectRetrieval = proj==-1 ? project : Project.read(proj)
                if(projectRetrieval) project.retrievalProjects.add(projectRetrieval)
            }
            project.save(flush: true)
        }
        return response
    }

    private void checkRetrievalConsistency(def json) {
        boolean retrievalDisable =  false
        if(!json.retrievalDisable.toString().equals("null")) retrievalDisable = Boolean.parseBoolean(json.retrievalDisable.toString())
        boolean retrievalAllOntology =  true
        if(!json.retrievalAllOntology.toString().equals("null")) retrievalAllOntology= Boolean.parseBoolean(json.retrievalAllOntology.toString())
        boolean retrievalProjectEmpty = true
        if(!json.retrievalProjects.toString().equals("null")) retrievalProjectEmpty = json.retrievalProjects.isEmpty()

        log.info "retrievalDisable=$retrievalDisable retrievalAllOntology=$retrievalAllOntology retrievalProjectEmpty=$retrievalProjectEmpty"

        if(retrievalDisable && retrievalAllOntology) throw new WrongArgumentException("Retrieval cannot be disable of all Projects are selected")
        if(retrievalDisable && !retrievalProjectEmpty) throw new WrongArgumentException("Retrieval cannot be disable of some Projects are selected")
        if(retrievalAllOntology && !retrievalProjectEmpty) throw new WrongArgumentException("Retrieval cannot be set for all procects if some projects are selected")
    }

    @PreAuthorize("#domain.hasPermission('WRITE') or hasRole('ROLE_ADMIN')")
    def update(def domain, def json) {
        checkRetrievalConsistency(json)
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

        Project project = Project.get(json.id)
        //update RetrievalProject
        if(!json.retrievalProjects.toString().equals("null")) {
            List<Long> newProjectRetrievalList = json.retrievalProjects.collect{
                Long.parseLong(it.toString())
            }
            List<Long> oldProjectRetrievalList = project.retrievalProjects.collect {it.id}
            log.info "newProjectRetrievalList = "  + newProjectRetrievalList
            log.info "oldProjectRetrievalList = "  + oldProjectRetrievalList

            List<Long> shouldBeAdded = CollectionUtils.subtract(newProjectRetrievalList,oldProjectRetrievalList)
            List<Long> shouldBeRemoved = CollectionUtils.subtract(oldProjectRetrievalList,newProjectRetrievalList)
            log.info "shouldBeAdded = " + shouldBeAdded
            log.info "shouldBeRemoved = " + shouldBeRemoved
            log.info "project="+project.retrievalProjects.collect{it.id}
            shouldBeAdded.each {
                log.info "add="+Project.read(it).id
                project.retrievalProjects.add(Project.read(it))

            }
            shouldBeRemoved.each {
                log.info "rem="+Project.read(it).id
                project.retrievalProjects.remove(Project.read(it))
            }
            log.info "project="+project.retrievalProjects.collect{it.id}
            project.save(flush: true)
            project.refresh()
            log.info "project="+project.retrievalProjects.collect{it.id}
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

        //remove Retrieval-project where this project is set
       def criteria = Project.createCriteria()
        List<Project> projectsThatUseThisProjectForRetrieval = criteria.list {
          retrievalProjects {
              eq('id', domain.id)
          }
        }

        projectsThatUseThisProjectForRetrieval.each {
            it.refresh()
            it.removeFromRetrievalProjects(domain)
            it.save(flush: true)
        }

        //remove retrieval-project of this project
        //domain.retrievalProjects.clear()

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
        UploadedFile.findAllByProject(domain).each { uploadedFile ->
            uploadedFile.delete()
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
