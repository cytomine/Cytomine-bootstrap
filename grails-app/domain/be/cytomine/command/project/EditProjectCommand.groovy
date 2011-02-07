package be.cytomine.command.project

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.Project

class EditProjectCommand extends Command implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)
    def updatedProject = Project.get(postData.project.id)
    def backup = updatedProject.encodeAsJSON()

    if (!updatedProject ) {
      return [data : [success : false, message : "Project not found with id: " + postData.project.id], status : 404]
    }

    for (property in postData.project) {
      updatedProject.properties.put(property.key, property.value)
    }


    if ( updatedProject.validate()) {
      data = ([ previousProject : (JSON.parse(backup)), newProject :  updatedProject]) as JSON
      updatedProject.save()
      return [data : [success : true, message:"ok", project :  updatedProject], status : 200]
    } else {
      return [data : [user :  updatedProject, errors : [ updatedProject.errors]], status : 403]
    }


  }

  def undo() {
    def usersData = JSON.parse(data)
    Project project = Project.findById(usersData.previousProject.id)
    project.name = usersData.previousProject.name
    project.save()
    return [data : [success : true, message:"ok", user : project], status : 200]
  }

  def redo() {
    def usersData = JSON.parse(data)
    Project project = Project.findById(usersData.newProject.id)
    project.name = usersData.newProject.name
    project.save()
    return [data : [success : true, message:"ok", user : project], status : 200]
  }


}
