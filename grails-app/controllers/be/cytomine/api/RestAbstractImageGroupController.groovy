package be.cytomine.api

import be.cytomine.command.Command
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import be.cytomine.image.AbstractImageGroup
import be.cytomine.command.abstractimagegroup.AddAbstractImageGroupCommand
import be.cytomine.command.abstractimagegroup.DeleteAbstractImageGroupCommand

class RestAbstractImageGroupController extends RestController {

    def springSecurityService

    def listGroupByAbstractImage = {
        if (params.idabstractimage == "undefined") responseNotFound("AbstractImageGroup", "AbstractImage", params.idabstractimage)
        else {
            AbstractImage abstractimage = AbstractImage.read(params.idabstractimage)
            if (abstractimage != null) responseSuccess(abstractimage.groups())
            else responseNotFound("AbstractImageGroup", "AbstractImage", params.idabstractimage)
        }

    }

    def listAbstractImageByGroup = {
        Group group = Group.read(params.idgroup)
        if (group != null) responseSuccess(group.abstractimages())
        else responseNotFound("AbstractImageGroup", "Group", params.idgroup)
    }


    def show = {
        AbstractImage abstractimage = AbstractImage.read(params.idabstractimage)
        Group group = Group.read(params.idgroup)
        if (abstractimage != null && group != null && AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group) != null)
            responseSuccess(AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group))
        else responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
    }


    def add = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new AddAbstractImageGroupCommand(user: currentUser), json)
        response(result)
    }

    def delete = {
        def json = ([abstractimage: params.idabstractimage, group: params.idgroup]) as JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new DeleteAbstractImageGroupCommand(user: currentUser), json)
        response(result)
    }
}