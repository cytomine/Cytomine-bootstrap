package be.cytomine.command.project

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON


class AddProjectCommand extends Command implements UndoRedoCommand {

  def execute() {
    def newProject = Project.getProjectFromData(JSON.parse(postData))
    if (newProject.validate()) {
      newProject.save()
      data = newProject.encodeAsJSON()
      return [data : [success : true, message:"ok", project : newProject], status : 201]
    } else {
      return [data : [project : newProject, errors : [newProject.errors]], status : 400]
    }
  }

  def undo() {
    def projectData = JSON.parse(data)
    def project = Project.findById(projectData.id)
    project.delete()
    return [data : null, status : 200]
  }

  def redo() {
    def projectData = JSON.parse(data)
    def project = Project.getUserFromData(JSON.parse(postData))
    project.id = projectData.id
    project.save()
    return [data : [project : project], status : 200]
  }


}
