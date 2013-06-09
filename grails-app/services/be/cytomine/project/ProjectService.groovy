package be.cytomine.project

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.ontology.*
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareProject
import be.cytomine.security.SecUser
import be.cytomine.social.LastConnection
import be.cytomine.social.UserPosition
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.*

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
    def imageSequenceService
    def propertyService

    def currentDomain() {
        Project
    }

    def read(def id) {
        def project = Project.read(id)
        if(project) {
            SecurityACL.check(project,READ)
        }
        project
    }

    def readMany(def ids) {
        def projects = Project.findAllByIdInList(ids)
        if(projects) {
            projects.each { project ->
                SecurityACL.check(project,READ)
            }
        }
        projects
    }

    def get(def id, def domain) {
        def project = Project.get(id)
        if(project) {
            SecurityACL.check(project,READ)
        }
        project
    }

    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        //list ALL projects,
        Project.list(sort: "name")
    }

    def list(SecUser user) {
        SecurityACL.checkUser(cytomineService.currentUser)
        //faster to get it from database table (getProjectList) than PostFilter
        SecurityACL.getProjectList(user)
    }

    def list(Ontology ontology) {
        //very slow method because it check right access for each project ontology
        SecurityACL.getProjectList(cytomineService.currentUser,ontology)
    }

    def list(Software software) {
        SecurityACL.getProjectList(cytomineService.currentUser,software)
    }

    def lastAction(Project project, def max) {
        SecurityACL.check(project, READ)
        return CommandHistory.findAllByProject(project, [sort: "created", order: "desc", max: max])
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkUser(currentUser)
        checkRetrievalConsistency(json)
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Project project, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(project.container(),WRITE)
        return executeCommand(new EditCommand(user: currentUser),project, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Project domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain,DELETE)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction,linkProject: false,refuseUndo:true)
        return executeCommand(c,domain,null)
    }

    def afterAdd(Project domain, def response) {
        log.info("Add permission on " + domain + " to " + springSecurityService.authentication.name)
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
    }


    protected def beforeDelete(Project domain) {
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
            imageFilterProjectService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentJob(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${Job.countByProject(project)} jobs":"")

        Job.findAllByProject(project).each {
            jobService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentSoftwareProject(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${SoftwareProject.countByProject(project)} links to software":"")

        SoftwareProject.findAllByProject(project).each {
            softwareProjectService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentAlgoAnnotation(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${AlgoAnnotation.countByProject(project)} annotations from algo":"")

        AlgoAnnotation.findAllByProject(project).each {
            algoAnnotationService.delete(it,transaction, null,false)
        }
    }

    def deleteDependentAlgoAnnotationTerm(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${AlgoAnnotationTerm.countByProject(project)} terms for annotation from algo":"")

        AlgoAnnotationTerm.findAllByProject(project).each {
            algoAnnotationTermService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentAnnotationFilter(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${AnnotationFilter.countByProject(project)} annotations filters":"")

        AnnotationFilter.findAllByProject(project).each {
            annotationFilterService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentImageInstance(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${ImageInstance.countByProject(project)} images":"")

        ImageInstance.findAllByProject(project).each {
            imageInstanceService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentReviewedAnnotation(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${ReviewedAnnotation.countByProject(project)} validate annotation":"")

        ReviewedAnnotation.findAllByProject(project).each {
            reviewedAnnotationService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentUserAnnotation(Project project, Transaction transaction,Task task=null) {

        taskService.updateTask(task,task? "Delete ${UserAnnotation.countByProject(project)} annotations created by user":"")

        UserAnnotation.findAllByProject(project).each {
            userAnnotationService.delete(it,transaction,null, false)
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

    def deleteDependentImageGroup(Project project, Transaction transaction, Task task = null) {
        ImageGroup.findAllByProject(project).each {
            imageSequenceService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentProperty(Project project, Transaction transaction, Task task = null) {
        Property.findAllByDomainIdent(project.id).each {
            propertyService.delete(it,transaction,null,false)
        }

    }
}
