package be.cytomine.command.project

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import be.cytomine.security.Group
import grails.converters.JSON

class EditProjectCommand extends EditCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = false;

    def execute() {
        //Retrieve domain
        Project updatedDomain = Project.get(json.id)
        if (!updatedDomain) throw new ObjectNotFoundException("Project ${json.id} not found")
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain, json)
        //Validate and save domain
        domainService.editDomain(updatedDomain,json)
        Group group = Group.findByName(updatedDomain.name)
        log.info "rename group " + updatedDomain.name + "(" + group + ") by " + json.name
        if (group) {
            group.name = json.name
            group.save(flush: true)
        }
        //Build response message
        String message = createMessage(updatedDomain, [updatedDomain.id, updatedDomain.name])
        //Init command info
        fillCommandInfo(updatedDomain,oldDomain,message)
        //Create and return response
        return responseService.createResponseMessage(updatedDomain,message,printMessage)

    }

    def undo() {
        return edit(projectService,JSON.parse(data).previousProject)
    }

    def redo() {
        return edit(projectService,JSON.parse(data).newProject)
    }
}
