package be.cytomine.command.project

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON

class AddProjectCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      Project newProject = Project.createProjectFromData(json)
      if (newProject.validate()) {
        newProject.save(flush:true)
        log.info("Save project with id:"+newProject.id)
        data = newProject.encodeAsJSON()
        return [data : [success : true, message:"ok", project : newProject], status : 201]
      } else {
        return [data : [project : newProject, errors : newProject.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save project:"+ex.toString())
      return [data : [project : null , errors : ["Cannot save project:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def projectData = JSON.parse(data)
    def project = Project.findById(projectData.id)
    project.delete(flush:true)
    log.debug("Delete project with id:"+projectData.id)
    return [data : ["Project deleted"], status : 200]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def projectData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def project = Project.createProjectFromData(json)
    project.id = projectData.id
    project.save(flush:true)
    log.debug("Save project:"+project.id)
    return [data : [project : project], status : 201]
  }

}
