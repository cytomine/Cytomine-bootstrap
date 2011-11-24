package be.cytomine.command.project

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup
import be.cytomine.security.Group
import be.cytomine.security.UserGroup
import grails.converters.JSON
import be.cytomine.command.*

class DeleteProjectCommand extends DeleteCommand implements UndoRedoCommand {

    def execute() {
        //Retrieve domain
        Project domain = Project.findById(json.id)
        if (!domain) throw new ObjectNotFoundException("Project $json.id not found!")
        //Delete all command / command history from project
        CommandHistory.findAllByProject(domain).each { it.delete() }
        Command.findAllByProject(domain).each {
            it
            UndoStackItem.findAllByCommand(it).each { it.delete()}
            RedoStackItem.findAllByCommand(it).each { it.delete()}
            it.delete()
        }
        //Delete group map with project
        Group projectGroup = Group.findByName(domain.name);

        if (projectGroup) {
            projectGroup.name = "TO REMOVE " + domain.id
            log.info "group " + projectGroup + " will be renamed"
            projectGroup.save(flush: true)
        }
        def groups = domain.groups()
        groups.each { group ->
            ProjectGroup.unlink(domain, group)
            //for each group, delete user link
            def users = group.users()
            users.each { user ->
                UserGroup.unlink(user, group)
            }
            //delete group
            group.delete(flush: true)
        }
        //Build response message
        String message = createMessage(domain, [domain.id, domain.name])
        //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        return restore(projectService,JSON.parse(data))
    }

    def redo() {
        return destroy(projectService,JSON.parse(data))
    }

}
