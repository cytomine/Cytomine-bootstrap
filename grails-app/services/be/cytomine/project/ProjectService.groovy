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
import be.cytomine.social.UserPosition
import be.cytomine.image.UploadedFile

import be.cytomine.social.LastConnection
import be.cytomine.utils.Task

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

    def currentDomain() {
        Project
    }

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

    private List<Project> getProjectList(SecUser user) {
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
        return executeCommand(new AddCommand(user: currentUser), json)
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
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectDelete() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) {
        return delete(retrieve(json),transactionService.start(),true,task)
    }

    def delete(Project project, Transaction transaction = null, boolean printMessage = true,Task task = null) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${project.id}}")

        /*
           linkProject must be false, special case because we delete project in this command
           If this command will be linked with the deleted project, we will have an database error (foreign key)
         */
        return executeCommand(new DeleteCommand(user: currentUser,linkProject:false,transaction:transaction,refuseUndo:true), json,task)
    }

    def afterAdd(Project domain, def response) {
        log.info("Add permission on " + domain + " to " + springSecurityService.authentication.name)
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
    }


    def beforeDelete(Project domain) {
        CommandHistory.findAllByProject(domain).each { it.delete() }
        Command.findAllByProject(domain).each {
            it
            UndoStackItem.findAllByCommand(it).each { it.delete()}
            RedoStackItem.findAllByCommand(it).each { it.delete()}
            it.delete()
        }
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
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

    def deleteDependentImageFilterProject(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${ImageFilterProject.countByProject(project)} links to image filter":"")

        ImageFilterProject.findAllByProject(project).each {
            imageFilterProjectService.delete(it,transaction, false)
        }
    }

    def deleteDependentJob(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${Job.countByProject(project)} jobs":"")

        Job.findAllByProject(project).each {
            jobService.delete(it,transaction, false)
        }
    }

    def deleteDependentSoftwareProject(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${SoftwareProject.countByProject(project)} links to software":"")

        SoftwareProject.findAllByProject(project).each {
            softwareProjectService.delete(it,transaction, false)
        }
    }

    def deleteDependentAlgoAnnotation(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${AlgoAnnotation.countByProject(project)} annotations from algo":"")

        AlgoAnnotation.findAllByProject(project).each {
            algoAnnotationService.delete(it,transaction, false)
        }
    }

    def deleteDependentAlgoAnnotationTerm(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${AlgoAnnotationTerm.countByProject(project)} terms for annotation from algo":"")

        AlgoAnnotationTerm.findAllByProject(project).each {
            algoAnnotationTermService.delete(it,transaction, false)
        }
    }

    def deleteDependentAnnotationFilter(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${AnnotationFilter.countByProject(project)} annotations filters":"")

        AnnotationFilter.findAllByProject(project).each {
            annotationFilterService.delete(it,transaction, false)
        }
    }

    def deleteDependentImageInstance(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${ImageInstance.countByProject(project)} images":"")

        ImageInstance.findAllByProject(project).each {
            imageInstanceService.delete(it,transaction, false)
        }
    }

    def deleteDependentReviewedAnnotation(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${ReviewedAnnotation.countByProject(project)} validate annotation":"")

        ReviewedAnnotation.findAllByProject(project).each {
            reviewedAnnotationService.delete(it,transaction, false)
        }
    }

    def deleteDependentUserAnnotation(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${UserAnnotation.countByProject(project)} annotations created by user":"")

        UserAnnotation.findAllByProject(project).each {
            userAnnotationService.delete(it,transaction, false)
        }
    }

    def deleteDependentHasManyProject(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Remove project from other project retrieval list":"")

        //remove Retrieval-project where this project is set
       def criteria = Project.createCriteria()
        List<Project> projectsThatUseThisProjectForRetrieval = criteria.list {
          retrievalProjects {
              eq('id', project.id)
          }
        }

        projectsThatUseThisProjectForRetrieval.each {
            it.refresh()
            it.removeFromRetrievalProjects(project)
            it.save(flush: true)
        }


        project.retrievalProjects?.clear()
    }

    def deleteDependentUserPosition(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${UserPosition.countByProject(project)} user position information":"")

        UserPosition.findAllByProject(project).each {
              it.delete()
        }
    }
//
//    def deleteDependentTask(Project project, Transaction transaction,Task task=null) {
//
//        taskService.updateTask(task,task? "Delete ${Task.countByProject(project)} user position information":"")
//
//        Task.findAllByProjectIdent(project).each {
//            //Task from param will loose project link too...
//            it.project = null
//        }
//    }

    def deleteDependentLastConnection(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${LastConnection.countByProject(project)} connection information":"")

        LastConnection.findAllByProject(project).each {
              it.delete()
        }
    }

}
