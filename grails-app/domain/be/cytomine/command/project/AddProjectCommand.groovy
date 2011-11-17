package be.cytomine.command.project

import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON
import be.cytomine.command.AddCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import org.codehaus.groovy.grails.web.json.JSONElement

class AddProjectCommand extends AddCommand implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    Project newProject=null
    try {
      //def json = JSON.parse(postData)
      //def json = JSON.parse(postData)
      log.info "json (execute) ="+json
      newProject = Project.createFromData(json)
      return super.validateAndSave(newProject,["#ID#",newProject.name] as Object[])
    }catch(ConstraintException  ex){
      return [data : [ontology:newProject,errors:newProject.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [ontology:null,errors:["Cannot save ontology:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def projectData = JSON.parse(data)
    Project project = Project.get(projectData.id)
    project.delete(flush:true)
    String id = projectData.id
    return super.createUndoMessage(id,project,[projectData.id,projectData.name] as Object[]);
  }

  def redo() {
    log.info("Undo")
    def projectData = JSON.parse(data)
    def project = Project.createFromData(projectData)
    project.id = projectData.id
    project.save(flush:true)
    return super.createRedoMessage(project,[projectData.id,projectData.name] as Object[]);
  }

}
