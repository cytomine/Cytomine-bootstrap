package be.cytomine.command.project

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.command.EditCommand
import be.cytomine.security.Group
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException

class EditProjectCommand extends EditCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = false;

    def execute() {
        Project updatedProject = Project.get(json.id)
        if (!updatedProject) throw new ObjectNotFoundException("Project $json.id not found!")
        Group group = Group.findByName(updatedProject.name)
        log.info "rename group " + updatedProject.name + "(" + group + ") by " + json.name
        if (group) {
            group.name = json.name
            group.save(flush: true)
        }
        return super.validateAndSave(json, updatedProject, [updatedProject.id, updatedProject.name] as Object[])

    }

    def undo() {
        log.info "Undo"
        def projectData = JSON.parse(data)
        Project project = Project.findById(projectData.previousProject.id)
        project = Project.getFromData(project, projectData.previousProject)
        project.save(flush: true)
        super.createUndoMessage(projectData, project, [project.id, project.name] as Object[])
    }

    def redo() {
        log.info "Redo"
        def projectData = JSON.parse(data)
        Project project = Project.findById(projectData.newProject.id)
        project = Project.getFromData(project, projectData.newProject)
        project.save(flush: true)
        super.createRedoMessage(projectData, project, [project.id, project.name] as Object[])
    }

}
