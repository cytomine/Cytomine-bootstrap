package be.cytomine.api

import be.cytomine.project.Discipline
import be.cytomine.Exception.CytomineException

class RestDisciplineController extends RestController {

    def springSecurityService
    def transactionService
    def disciplineService

    def list = {
        responseSuccess(disciplineService.list())
    }

    def show = {
        Discipline discipline = disciplineService.read(params.id)
        if(discipline) responseSuccess(discipline)
        else responseNotFound("Discipline",params.id)
    }

    def add = {
        try {
            def result = disciplineService.add(request.JSON)
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
            def result = disciplineService.update(request.JSON)
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
            def result = disciplineService.delete(params.id)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

}
