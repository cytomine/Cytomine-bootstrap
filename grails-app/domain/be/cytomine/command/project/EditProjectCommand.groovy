package be.cytomine.command.project

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.command.EditCommand
import be.cytomine.security.Group
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class EditProjectCommand extends EditCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info "Execute"
    log.debug "postData="+postData
    Project updatedProject=null
    try {
      def postData = JSON.parse(postData)
      updatedProject = Project.get(postData.id)
      Group group = Group.findByName(updatedProject.name)
      log.info "rename group " + updatedProject.name + "("+group+") by " + postData.name
      if(group){
        group.name = postData.name
        group.save(flush:true)
      }
      return super.validateAndSave(postData,updatedProject,[updatedProject.id,updatedProject.name] as Object[])

    } catch(NullPointerException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 404]
    } catch(ConstraintException e) {
      log.error(e)
      return [data : [success : false, errors : updatedProject.retrieveErrors()], status : 400]
    } catch(IllegalArgumentException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 400]
    }

  }

  def undo() {
    log.info "Undo"
    def projectData = JSON.parse(data)
    Project project = Project.findById(projectData.previousProject.id)
    project = Project.getFromData(project,projectData.previousProject)
    project.save(flush:true)
    super.createUndoMessage(projectData, project,[project.id,project.name] as Object[])
  }

  def redo() {
    log.info "Redo"
    def projectData = JSON.parse(data)
    Project project = Project.findById(projectData.newProject.id)
    project = Project.getFromData(project,projectData.newProject)
    project.save(flush:true)
    super.createRedoMessage(projectData, project,[project.id,project.name] as Object[])
  }

}
