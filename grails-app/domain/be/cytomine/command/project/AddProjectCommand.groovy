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
        log.info("Undo")
        def projectData = JSON.parse(data)
        Project project = Project.get(projectData.id)
        project.delete(flush: true)
        String id = projectData.id
        return super.createUndoMessage(id, project, [projectData.id, projectData.name] as Object[]);
    }

    def redo() {
        log.info("Undo")
        def projectData = JSON.parse(data)
        def project = Project.createFromData(projectData)
        project.id = projectData.id
        project.save(flush: true)
        return super.createRedoMessage(project, [projectData.id, projectData.name] as Object[]);
    }

}
