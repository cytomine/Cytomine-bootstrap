package be.cytomine.command.project

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON

class DeleteProjectCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)

    Project project = Project.findById(postData.id)
    data = project.encodeAsJSON()

    if (!project) {
      return [data : [success : false, message : "Project not found with id: " + postData.id], status : 404]
    }

    project.delete();
    return [data : [success : true, message : "OK", data : [project : postData.id]], status : 204]
  }

  def undo() {
    def projectData = JSON.parse(data)
    Project project = new Project(projectData)
    project.save()
    return [data : [success : true, project : project, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)
    Project project = Project.findById(postData.id)
    project.delete();
    return [data : [success : true, message : "OK"], status : 204]

  }


}
