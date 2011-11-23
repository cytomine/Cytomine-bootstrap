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
        Project project = Project.findById(json.id)
        if (!project) throw new ObjectNotFoundException("Project $json.id not found!")

        log.info "project " + project + " " + project?.name + " will be deleted"

        //Delete all command / command history from project
        CommandHistory.findAllByProject(project).each { it.delete() }
        Command.findAllByProject(project).each {
            it
            UndoStackItem.findAllByCommand(it).each { it.delete()}
            RedoStackItem.findAllByCommand(it).each { it.delete()}
            it.delete()
        }

        //Delete group map with project
        Group projectGroup = Group.findByName(project.name);

        if (projectGroup) {
            projectGroup.name = "TO REMOVE " + project.id
            log.info "group " + projectGroup + " will be renamed"
            projectGroup.save(flush: true)
        }
        def groups = project.groups()
        groups.each { group ->
            ProjectGroup.unlink(project, group)
            //for each group, delete user link
            def users = group.users()
            users.each { user ->
                UserGroup.unlink(user, group)
            }
            //delete group
            group.delete(flush: true)
        }
        return super.deleteAndCreateDeleteMessage(json.id, project, [project.id, project.name] as Object[])
    }

    def undo() {
        log.info("Undo")
        def projectData = JSON.parse(data)
        Project project = Project.createFromData(projectData)
        project.id = projectData.id;
        project.save(flush: true)
        return super.createUndoMessage(project, [project.id, project.name] as Object[]);
    }

    def redo() {
        log.info("Redo")
        def postData = JSON.parse(postData)
        Project project = Project.findById(postData.id)
        project.delete(flush: true);
        String id = postData.id
        return super.createRedoMessage(id, project, [postData.id, postData.name] as Object[]);
    }

}
