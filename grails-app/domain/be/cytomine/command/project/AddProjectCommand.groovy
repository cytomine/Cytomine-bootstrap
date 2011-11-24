package be.cytomine.command.project

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON

class AddProjectCommand extends AddCommand implements UndoRedoCommand {

    def execute() {
        //Init new domain object
        Project domain = Project.createFromData(json)
        //Validate and save domain
        domainService.saveDomain(domain)
        //Build response message
        String message = createMessage(domain, [domain.id, domain.name])
        //Init command info
        fillCommandInfo(domain,message)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        return destroy(projectService,JSON.parse(data))
    }

    def redo() {
        return restore(projectService,JSON.parse(data))
    }

}
