package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.Exception.CytomineException
import be.cytomine.processing.Software

class RestProjectController extends RestController {

    def springSecurityService
    def projectService
    def userService
    def disciplineService
    def ontologyService
    def cytomineService
    def transactionService
    def retrievalService
    def imageInstanceService

    def list = {
        responseSuccess(projectService.list())
    }

    def listBySoftware = {
        Software software = Software.read(params.long('id'))
        if(software) responseSuccess(projectService.list(software))
        else responseNotFound("Software", params.id)
    }

    def listByOntology = {
        log.info "listByOntology with ontology id:" + params.id
        Ontology ontology = ontologyService.read(params.long('id'));
        if (ontology != null) responseSuccess(projectService.list(ontology))
        else responseNotFound("Project", "Ontology", params.id)
    }

    def listByUser = {
        log.info "List with id user:" + params.id
        User user = User.read(params.long('id'))
        if(user) responseSuccess(projectService.list(user))
        else responseNotFound("User", params.id)
    }

    def listByDiscipline = {
        log.info "listByDiscipline with discipline id:" + params.id
        Discipline discipline = disciplineService.read(params.long('id'));
        if (discipline) responseSuccess(projectService.list(discipline))
        else responseNotFound("Project", "Discipline", params.id)
    }

    def show = {
        Project project = projectService.read(params.long('id'), new Project())
        if (project) {
            log.info project.users()
            def userList = project.users().collect{it.id}
            def currentUserAuth = cytomineService.getCurrentUser().authorities.asList().collect{it.authority}
            log.info "check authorization: project="+ project.id + " user="+  cytomineService.getCurrentUser().id + " user-project="+userList  + " user-type="+currentUserAuth
            projectService.checkAuthorization(project)
            responseSuccess(project)
        }
        else responseNotFound("Project", params.id)
    }

    def lastAction = {
        log.info "lastAction"
        Project project = projectService.read(params.long('id'),new Project())    //need to be filter by project
        int max = Integer.parseInt(params.max);

        if (project)
            responseSuccess(projectService.lastAction(project, max))
        else responseNotFound("Project", params.id)
    }

    def add = {
        add(projectService, request.JSON)
    }

    def update = {
        update(projectService, request.JSON)
    }

//    def delete = {
//        delete(projectService, JSON.parse("{id : $params.id}"))
//    }


    def delete = {
        try {
            def domain = projectService.retrieve(JSON.parse("{id : $params.id}"))
            def result = projectService.delete(domain,JSON.parse("{id : $params.id}"))
            log.info "delete container $params.id start"
            try {retrievalService.deleteContainerAsynchronous(params.id) } catch(Exception e) {log.error e}
            log.info "delete container $params.id in progress"
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def showPreview = {
        int inf = 0
        if (params.inf != null) inf = Integer.parseInt(params.inf)
        int sup = inf + 1
        Project project = projectService.read(params.long('id'), new Project())
        String previewURL = imageInstanceService.list(project, inf, sup).first().getBaseImage().getThumbURL()
        responseImage(previewURL)
    }
}

