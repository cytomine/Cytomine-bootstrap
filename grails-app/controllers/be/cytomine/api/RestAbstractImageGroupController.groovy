package be.cytomine.api

import grails.converters.JSON
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.Exception.CytomineException

class RestAbstractImageGroupController extends RestController {

    def abstractImageService
    def abstractImageGroupService
    def transactionService
    def groupService

    def show = {
        AbstractImage abstractimage = abstractImageService.read(params.idabstractimage)
        Group group = groupService.read(params.idgroup)
        if (abstractimage && group) {
            def abstractimageGroup =  abstractImageGroupService.get(abstractimage,group)
            if(abstractimageGroup) responseSuccess(abstractimageGroup)
            else responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
        }
        else responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
    }

    def add = {
        try {
            def result = abstractImageGroupService.add(request.JSON)
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
            def json = JSON.parse("{abstractimage: $params.idabstractimage, group: $params.idgroup}")
            def result = abstractImageGroupService.delete(json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }
}