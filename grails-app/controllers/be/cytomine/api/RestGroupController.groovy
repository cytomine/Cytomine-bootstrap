package be.cytomine.api

import be.cytomine.security.Group
import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.command.user.AddUserCommand
import be.cytomine.command.group.AddGroupCommand
import grails.converters.JSON

class RestGroupController extends  RestController {

    def list = {
        responseSuccess(Group.list())
    }

    def show = {
        Group group = Group.read(params.id)
        if (group) responseSuccess(group)
        else responseNotFound("Group", params.id)
    }

    def save = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        Command addGroupCommand = new AddGroupCommand(postData: request.JSON.toString(), user: currentUser)
        def result = processCommand(addGroupCommand, currentUser)
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
