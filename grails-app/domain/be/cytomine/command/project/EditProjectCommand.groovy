package be.cytomine.command.project

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.Project

class EditProjectCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info "Execute"
    log.debug "postData="+postData

    try {
      def postData = JSON.parse(postData)
      log.debug "Project id="+postData.project.id
      def updatedProject = Project.get(postData.project.id)
      def backup = updatedProject.encodeAsJSON()

      if (!updatedProject ) {
        log.error "Project not found with id: " + postData.project.id
        return [data : [success : false, message : "Project not found with id: " + postData.project.id], status : 404]
      }

      updatedProject = Project.getProjectFromData(updatedProject,postData.project)
      updatedProject.id = postData.project.id

      if ( updatedProject.validate() && updatedProject.save(flush:true)) {
        log.info "New project is saved"
        data = ([ previousProject : (JSON.parse(backup)), newProject :  updatedProject]) as JSON
        return [data : [success : true, message:"ok", project :  updatedProject], status : 200]
      } else {
        log.error "New projectcan't be saved: " +  updatedProject.errors
        return [data : [project :  updatedProject, errors :  updatedProject.retrieveErrors()], status : 400]
      }
    }
    catch(IllegalArgumentException e)
    {
      log.error "New Project can't be saved: " +  e.toString()
      return [data : [project : null , errors : [e.toString()]], status : 400]
    }
  }



  def undo() {
    log.info "Undo"
    def projectData = JSON.parse(data)
    Project project = Project.findById(projectData.previousProject.id)
    project = Project.getProjectFromData(project,projectData.previousProject)
    project.save(flush:true)
    return [data : [success : true, message:"ok", project : project], status : 200]
  }

  def redo() {
    log.info "Redo"
    def projectData = JSON.parse(data)
    Project project = Project.findById(projectData.newProject.id)
    project = Project.getProjectFromData(project,projectData.newProject)
    project.save(flush:true)
    return [data : [success : true, message:"ok", project : project], status : 200]
  }





}
