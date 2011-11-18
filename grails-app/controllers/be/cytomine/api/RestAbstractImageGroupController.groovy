package be.cytomine.api

import be.cytomine.command.Command
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.image.AbstractImageGroup
import be.cytomine.command.abstractimagegroup.AddAbstractImageGroupCommand
import be.cytomine.command.abstractimagegroup.DeleteAbstractImageGroupCommand
import be.cytomine.Exception.CytomineException

class RestAbstractImageGroupController extends RestController {

    def springSecurityService
    def abstractImageService
    def abstractImageGroupService
    def transactionService

    def show = {
        AbstractImage abstractimage = abstractImageService.read(params.idabstractimage)
        Group group = Group.read(params.idgroup)
        if (abstractimage != null && group != null) {
            def abstractimageGroup =  abstractImageGroupService.get(abstractimage,group)
            if(abstractimageGroup) responseSuccess(abstractimageGroup)
            else responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
        }
        else responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
    }

    def add = {
        try {
            def result = abstractImageGroupService.addAbstractImageGroup(request.JSON)
            responseOK(result)
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
            def result = abstractImageGroupService.deleteAbstractImageGroup(json)
            responseOK(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }
}