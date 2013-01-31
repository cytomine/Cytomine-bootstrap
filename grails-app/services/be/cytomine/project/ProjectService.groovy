package be.cytomine.project

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SecurityCheck

import be.cytomine.ontology.Ontology
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.Software
import be.cytomine.security.SecUser
import be.cytomine.security.User

import be.cytomine.utils.ModelService
import groovy.sql.Sql
import org.apache.commons.collections.CollectionUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import be.cytomine.command.*
import be.cytomine.processing.Job
import be.cytomine.processing.SoftwareProject
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import grails.converters.JSON

class ProjectService extends ModelService {

    static transactional = true
    def cytomineService
    def modelService
    def springSecurityService
    def aclUtilService
    def dataSource
    def imageFilterProjectService
    def jobService
    def softwareProjectService
    def transactionService
    def algoAnnotationService
    def algoAnnotationTermService
    def annotationFilterService
    def imageInstanceService
    def reviewedAnnotationService
    def userAnnotationService

    final boolean saveOnUndoRedoStack = false

    def read(def id) {
        def project = Project.read(id)
        if(project) {
            SecurityCheck.checkReadAuthorization(project)
        }
        project
    }

    def get(def id, def domain) {
        def project = Project.get(id)
        if(project) {
            project.checkReadPermission()
        }
        project
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        //list ALL projects,
        Project.list(sort: "name")
    }

    @PostFilter("filterObject.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Ontology ontology) {
        //very slow method because it check right access for each project ontology
        Project.findAllByOntology(ontology)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def list(User user) {
        //faster to get it from database table (getProjectList) than PostFilter
        getProjectList(user)
    }

    @PostFilter("filterObject.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Software software) {
        SoftwareProject.findAllBySoftware(software).collect {it.project}
    }

    List<Project> getProjectList(SecUser user) {
        //faster method
        return Project.executeQuery(
                "select distinct project "+
                "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser, Project as project "+
                "where aclObjectId.objectId = project.id " +
                "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
    }


    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def lastAction(Project project, def max) {
        return CommandHistory.findAllByProject(project, [sort: "created", order: "desc", max: max])
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        checkRetrievalConsistency(json)
        def response = executeCommand(new AddCommand(user: currentUser), json)

        //add or delete RetrievalProject
        Project project = response.object
        if(!json.retrievalProjects.toString().equals("null")) {
            project.refresh()
            json.retrievalProjects.each { idProject ->
                Long proj = Long.parseLong(idProject.toString())
                Project projectRetrieval = proj==-1 ? project : Project.read(proj)
                if(projectRetrieval) {
                    project.retrievalProjects.add(projectRetrieval)
                }
            }
            project.save(flush: true)
        }
        return response
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkProjectWrite() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {

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

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectDelete() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {
        return delete(retrieve(json),transactionService.start())
    }

    def delete(Project project, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${project.id}}")
        /*
           linkProject must be false, special case because we delete project in this command
           If this command will be linked with the deleted project, we will have an database error (foreign key)
         */
        return executeCommand(new DeleteCommand(user: currentUser,linkProject:false), json)
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(Project.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(Project domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        log.info("Add permission on " + domain + " to " + springSecurityService.authentication.name)
        //add permission on project
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
//        aclUtilService.addPermission(domain.ontology, cytomineService.currentUser.username, BasePermission.READ)
//        aclUtilService.addPermission(domain.ontology, cytomineService.currentUser.username, BasePermission.WRITE)
//        aclUtilService.addPermission(domain.ontology, cytomineService.currentUser.username, BasePermission.DELETE)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Project.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(Project domain, boolean printMessage) {
        //Build response message
        log.info "destroy project"
//
//        //remove Retrieval-project where this project is set
//       def criteria = Project.createCriteria()
//        List<Project> projectsThatUseThisProjectForRetrieval = criteria.list {
//          retrievalProjects {
//              eq('id', domain.id)
//          }
//        }
//
//        projectsThatUseThisProjectForRetrieval.each {
//            it.refresh()
//            it.removeFromRetrievalProjects(domain)
//            it.save(flush: true)
//        }
//
//        ImageFilterProject.findAllByProject(domain).each {
//            it.delete()
//        }
//
//        //remove retrieval-project of this project
//        //domain.retrievalProjects.clear()
//
        //Delete all command / command history from project
        CommandHistory.findAllByProject(domain).each { it.delete() }
        Command.findAllByProject(domain).each {
            it
            UndoStackItem.findAllByCommand(it).each { it.delete()}
            RedoStackItem.findAllByCommand(it).each { it.delete()}
            it.delete()
        }
//        log.info "command deleted"
//        UploadedFile.findAllByProject(domain).each { uploadedFile ->
//            uploadedFile.delete()
//        }
//
//        LastConnection.findAllByProject(domain).each {
//            it.delete()
//        }
//
//        UserPosition.findAllByProject(domain).each {
//            it.delete()
//        }
//
//        Task.findAllByProject(domain).each {
//            it.delete()
//        }

        log.info "createResponseMessage"
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        log.info "deleteDomain"
        deleteDomain(domain)
        log.info "response"
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Project(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(Project project, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(project, [project.id, project.name], printMessage, "Edit", project.getCallBack())
        //Save update
        saveDomain(project)
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

    /**
     * Check if retrieval parameter from a project json are ok
     * E.g. if retrieval is disable and project retrieval is not empty, there is a mistake
     * @param json Project json
     */
    private void checkRetrievalConsistency(def json) {

        boolean retrievalDisable =  false
        if(!json.retrievalDisable.toString().equals("null")) {
            retrievalDisable = Boolean.parseBoolean(json.retrievalDisable.toString())
        }

        boolean retrievalAllOntology =  true
        if(!json.retrievalAllOntology.toString().equals("null")) {
            retrievalAllOntology= Boolean.parseBoolean(json.retrievalAllOntology.toString())
        }

        boolean retrievalProjectEmpty = true
        if(!json.retrievalProjects.toString().equals("null")) {
            retrievalProjectEmpty = json.retrievalProjects.isEmpty()
        }

        if(retrievalDisable && retrievalAllOntology) {
            throw new WrongArgumentException("Retrieval cannot be disable of all Projects are selected")
        }
        if(retrievalDisable && !retrievalProjectEmpty) {
            throw new WrongArgumentException("Retrieval cannot be disable of some Projects are selected")
        }
        if(retrievalAllOntology && !retrievalProjectEmpty) {
            throw new WrongArgumentException("Retrieval cannot be set for all procects if some projects are selected")
        }
    }

    def deleteDependentImageFilterProject(Project project, Transaction transaction) {
        ImageFilterProject.findAllByProject(project).each {
            imageFilterProjectService.delete(it,transaction, false)
        }
    }

    def deleteDependentJob(Project project, Transaction transaction) {
        Job.findAllByProject(project).each {
            jobService.delete(it,transaction, false)
        }
    }

    def deleteDependentSoftwareProject(Project project, Transaction transaction) {
        SoftwareProject.findAllByProject(project).each {
            softwareProjectService.delete(it,transaction, false)
        }
    }

    def deleteDependentAlgoAnnotation(Project project, Transaction transaction) {
        AlgoAnnotation.findAllByProject(project).each {
            algoAnnotationService.delete(it,transaction, false)
        }
    }

    def deleteDependentAlgoAnnotationTerm(Project project, Transaction transaction) {
        AlgoAnnotationTerm.findAllByProject(project).each {
            algoAnnotationTermService.delete(it,transaction, false)
        }
    }

    def deleteDependentAnnotationFilter(Project project, Transaction transaction) {
        AnnotationFilter.findAllByProject(project).each {
            annotationFilterService.delete(it,transaction, false)
        }
    }

    def deleteDependentImageInstance(Project project, Transaction transaction) {
        ImageInstance.findAllByProject(project).each {
            imageInstanceService.delete(it,transaction, false)
        }
    }

    def deleteDependentReviewedAnnotation(Project project, Transaction transaction) {
        ReviewedAnnotation.findAllByProject(project).each {
            reviewedAnnotationService.delete(it,transaction, false)
        }
    }

    def deleteDependentUserAnnotation(Project project, Transaction transaction) {
        UserAnnotation.findAllByProject(project).each {
            userAnnotationService.delete(it,transaction, false)
        }
    }
}
