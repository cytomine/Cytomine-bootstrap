package be.cytomine.api

import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.ontology.Ontology
import be.cytomine.project.Discipline
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
        Ontology ontology = ontologyService.read(params.id);
        if(ontology != null) responseSuccess(projectService.list(ontology))
        else responseNotFound("Project","Ontology",params.id)
    }

    def listByUser = {
        log.info "List with id user:"+params.id + " (null will be currentuser)"
        User user=null
        if(params.id != null)
            user = User.read(params.id)
        else
            user = cytomineService.getCurrentUser()

        if(user) responseSuccess(projectService.list(user))
        else responseNotFound("User",params.id)
    }

    def listByDiscipline = {
        log.info "listByDiscipline with discipline id:" + params.id
        Discipline discipline = disciplineService.read(params.id);
        if(discipline) responseSuccess(projectService.list(discipline))
        else responseNotFound("Project","Discipline",params.id)
    }

    def show = {
        Project project = projectService.read(params.id)
        if(project) responseSuccess(project)
        else responseNotFound("Project", params.id)
    }

    def lastAction = {
        log.info "lastAction"
        Project project = projectService.read(params.id)    //need to be filter by project
        int max =  Integer.parseInt(params.max);

        if(project)
            responseSuccess(projectService.lastAction(project, max))
        else responseNotFound("Project", params.id)
    }


    def add = {
        try {
            def result = projectService.add(request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def update = {
        try {
            def result = projectService.update(request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def delete = {
        try {
            def result = projectService.delete(params.id)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }
}

