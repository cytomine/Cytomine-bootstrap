package be.cytomine.api

import be.cytomine.security.Group
import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.command.group.AddGroupCommand
import grails.converters.JSON
import be.cytomine.command.group.EditGroupCommand
import be.cytomine.command.group.DeleteGroupCommand
import be.cytomine.image.AbstractImage
import be.cytomine.GroupService

class RestGroupController extends  RestController {
    def abstractImageService
    def groupService

    def list = {
        responseSuccess(Group.list(sort:"name", order:"asc"))
    }

    def listGroupByAbstractImage = {
        if (params.idabstractimage == "undefined") responseNotFound("AbstractImageGroup", "AbstractImage", params.idabstractimage)
        else {
            AbstractImage abstractimage = abstractImageService.read(params.idabstractimage)
            if (abstractimage != null) responseSuccess(groupService.list(abstractimage))
            else responseNotFound("AbstractImageGroup", "AbstractImage", params.idabstractimage)
        }
    }


    def show = {
        Group group = Group.read(params.id)
        if (group) responseSuccess(group)
        else responseNotFound("Group", params.id)
    }

    def save = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new AddGroupCommand(user: currentUser), json)
        response(result)
    }
    
    def update = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new EditGroupCommand(user: currentUser), json)
        response(result)
    }

    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def jsonData = JSON.parse("{id : $params.id}")
        def result = processCommand(new DeleteGroupCommand(user: currentUser), jsonData)
        response(result)
    }

     def grid = {
        def sortIndex = params.sidx ?: 'id'
        def sortOrder  = params.sord ?: 'asc'
        def maxRows = 50//params.row ? Integer.valueOf(params.rows) : 20
        def currentPage = params.page ? Integer.valueOf(params.page) : 1
        def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
        def groups = Group.createCriteria().list(max: maxRows, offset: rowOffset) {
            if (params.name)
                ilike('name', "%${params.name}%")

            order(sortIndex, sortOrder).ignoreCase()
        }

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
