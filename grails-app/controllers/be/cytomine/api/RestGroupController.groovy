package be.cytomine.api

import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.command.group.AddGroupCommand
import grails.converters.JSON
import be.cytomine.command.group.EditGroupCommand
import be.cytomine.command.group.DeleteGroupCommand
import be.cytomine.image.AbstractImage
import be.cytomine.Exception.CytomineException

class RestGroupController extends  RestController {
    def abstractImageService
    def groupService
    def transactionService

    def list = {
        responseSuccess(groupService.list())
    }

    def listGroupByAbstractImage = {
        if (params.idabstractimage == "undefined") responseNotFound("AbstractImageGroup", "AbstractImage", params.idabstractimage)
        else {
            AbstractImage abstractimage = abstractImageService.read(params.idabstractimage)
            if (abstractimage) responseSuccess(groupService.list(abstractimage))
            else responseNotFound("AbstractImageGroup", "AbstractImage", params.idabstractimage)
        }
    }

    def show = {
        Group group = groupService.read(params.id)
        if (group) responseSuccess(group)
        else responseNotFound("Group", params.id)
    }

    def add = {
        try {
            def result = groupService.add(request.JSON)
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
            def result = groupService.update(request.JSON)
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
            def result = groupService.delete(params.id)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

     def grid = {
        def sortIndex = params.sidx ?: 'id'
        def sortOrder  = params.sord ?: 'asc'
        def maxRows = 50//params.row ? Integer.valueOf(params.rows) : 20
        def currentPage = params.page ? Integer.valueOf(params.page) : 1
        def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

        def groups = groupService.list(sortIndex,sortOrder,maxRows,currentPage,rowOffset,params.name)

        def totalRows = groups.totalCount
        def numberOfPages = Math.ceil(totalRows / maxRows)

        def results = groups?.collect {
            [
                    name : it.name,
                    id: it.id
            ]
        }

        def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
        render jsonData as JSON
    }

}
