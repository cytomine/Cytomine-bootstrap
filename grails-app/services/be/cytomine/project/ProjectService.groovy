package be.cytomine.project

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.ModelService
import be.cytomine.image.UploadedFile
import be.cytomine.ontology.Ontology
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.Software
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.LastConnection
import be.cytomine.social.UserPosition
import be.cytomine.test.Infos
import org.apache.commons.collections.CollectionUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission
import org.springframework.transaction.annotation.Transactional
import be.cytomine.command.*
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.SecurityCheck

class ProjectService extends ModelService {

    static transactional = true
    def cytomineService
    def domainService
    def springSecurityService
    def securityService
    def userService
    def aclUtilService


    final boolean saveOnUndoRedoStack = false



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
        securityService.getProjectList(user)
    }

    //TODO: should be optim!!!!
    def list(AbstractImage image) {
        return ImageInstance.findAllByBaseImage(image).collect{it.project}.unique()
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
    def add(def json,SecurityCheck security) {
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
                log.info "idProject="+idProject
                Long proj = Long.parseLong(idProject.toString())
                log.info "proj="+proj
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

    @PreAuthorize("#security.checkProjectWrite() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
//        println "hasPermission="+domain.hasPermission("READ")
//        println "hasPermission="+domain.hasPermission('WRITE')
//        throw new Exception()

        checkRetrievalConsistency(json)
        SecUser currentUser = cytomineService.getCurrentUser()
        def response = executeCommand(new EditCommand(user: currentUser), json)
        //Validate and save domain

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

    @PreAuthorize("#security.checkProjectDelete() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        /*
           linkProject must be false, special case because we delete project in this command
           If this command will be linked with the deleted project, we will have an database error (foreign key)
         */
        DeleteCommand command = new DeleteCommand(user: currentUser, linkProject:false)
        return executeCommand(command, json)
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

        log.info "#project.hasPermission('ADMIN')="+domain.hasPermission('ADMIN')
        log.info "#admin="+cytomineService.currentUser.getAuthorities().collect {it.authority}
        log.info "#domain.isCurrentUserCreator()="+domain.isCurrentUserCreator()
        log.info "#Creator="+domain.retrieveCreator()?.id
        log.info "#Creator="+cytomineService.currentUser?.id
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(domain.ontology, cytomineService.currentUser.username, BasePermission.READ)
        aclUtilService.addPermission(domain.ontology, cytomineService.currentUser.username, BasePermission.WRITE)
        aclUtilService.addPermission(domain.ontology, cytomineService.currentUser.username, BasePermission.DELETE)
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
        log.info "destroy project"

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

        ImageFilterProject.findAllByProject(domain).each {
            it.delete()
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
        UploadedFile.findAllByProject(domain).each { uploadedFile ->
            uploadedFile.delete()
        }

        LastConnection.findAllByProject(domain).each {
            it.delete()
        }

        UserPosition.findAllByProject(domain).each {
            it.delete()
        }

        Task.findAllByProject(domain).each {
            it.delete()
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
