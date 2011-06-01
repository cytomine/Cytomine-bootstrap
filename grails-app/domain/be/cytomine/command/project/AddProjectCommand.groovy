package be.cytomine.command.project

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON
import be.cytomine.command.AddCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddProjectCommand extends AddCommand implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    Project newProject
    try {
      def json = JSON.parse(postData)
      newProject = Project.createProjectFromData(json)
      return super.validateAndSave(newProject,"Project",["#ID#",json.name] as Object[])
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

    log.info ("termData="+projectData)

    String id = projectData.id

    return super.createUndoMessage(
            id,
            'Ontology',
            [projectData.id,projectData.name] as Object[]
    );
  }

  def redo() {
    log.info("Undo")
    def projectData = JSON.parse(data)
    def json = JSON.parse(postData)
    def project = Project.createProjectFromData(json)
    project.id = projectData.id
    project.save(flush:true)
    return super.createRedoMessage(
            project,
            'Project',
            [projectData.id,projectData.name] as Object[]
    );
  }

}
