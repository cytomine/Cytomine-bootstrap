package be.cytomine.command.project

import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON
import be.cytomine.command.AddCommand

class AddProjectCommand extends AddCommand implements UndoRedoCommand {

    def execute() {
        Project newProject = Project.createFromData(json)
        return super.validateAndSave(newProject, ["#ID#", newProject.name] as Object[])
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
