package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.Exception.CytomineException

class RestProjectController extends RestController {

    def springSecurityService
    def projectService
    def userService
    def disciplineService
    def ontologyService
    def cytomineService
    def transactionService

    def list = {
        responseSuccess(projectService.list())
    }

    def listByOntology = {
        log.info "listByOntology with ontology id:" + params.id
        Ontology ontology = ontologyService.read(params.long('id'));
        if (ontology != null) responseSuccess(projectService.list(ontology))
        else responseNotFound("Project", "Ontology", params.id)
    }

    def listByUser = {
        log.info "List with id user:" + params.id + " (null will be currentuser)"
        User user = null
        if (params.id != null) {
            user = User.read(params.long('id'))
            if(user) responseSuccess(projectService.list(user))
            else responseNotFound("User", params.id)
        }
        else
            responseSuccess(projectService.list())
    }

    def listByDiscipline = {
        log.info "listByDiscipline with discipline id:" + params.id
        Discipline discipline = disciplineService.read(params.long('id'));
        if (discipline) responseSuccess(projectService.list(discipline))
        else responseNotFound("Project", "Discipline", params.id)
    }

    def show = {
        Project project = projectService.read(params.long('id'))
        if (project) responseSuccess(project)
        else responseNotFound("Project", params.id)
    }

    def lastAction = {
        log.info "lastAction"
        Project project = projectService.read(params.long('id'))    //need to be filter by project
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

    def delete = {
        delete(projectService, JSON.parse("{id : $params.id}"))
    }

//    def delete = {
//        try {
//            def result = projectService.delete(Long.parseLong(params.id))
//            responseResult(result)
//        } catch (CytomineException e) {
//            log.error(e)
//            response([success: false, errors: e.msg], e.code)
//        } finally {
//            transactionService?.stopIfTransactionInProgress()
//        }
//    }
}

