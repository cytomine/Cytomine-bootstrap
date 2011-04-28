package be.cytomine.command.projectslide

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.ProjectSlide
import be.cytomine.project.Project
import be.cytomine.project.Slide

class AddProjectSlideCommand extends AddCommand implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      ProjectSlide newProjectSlide = ProjectSlide.createProjectSlideFromData(json)
      if (newProjectSlide.validate()) {
        newProjectSlide =  ProjectSlide.link(newProjectSlide.project,newProjectSlide.slide)
        log.info("Save ProjectSlide with id:"+newProjectSlide.id)
        data = newProjectSlide.encodeAsJSON()
        return [data : [success : true, message:"ok", projectSlide : newProjectSlide], status : 201]
      } else {
        return [data : [projectSlide : newProjectSlide, errors : newProjectSlide.retrieveErrors()], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save projectSlide:"+ex.toString())
      return [data : [projectSlide : null , errors : ["Cannot save projectSlide:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def projectSlideData = JSON.parse(data)
    def projectSlide = ProjectSlide.findByProjectAndSlide(Project.get(projectSlideData.project),Slide.get(projectSlideData.slide))
    ProjectSlide.unlink(projectSlide.project,projectSlide.slide)
    log.debug("Delete projectSlide with id:"+projectSlideData.id)
    return [data : ["ProjectSlide deleted"], status : 200]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def projectSlideData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def projectSlide = ProjectSlide.createProjectSlideFromData(json)
    projectSlide = ProjectSlide.link(projectSlideData.id,projectSlide.project,projectSlide.slide)
    //println "projectSlideData.id="+projectSlideData.id

    log.debug("Save projectSlide:"+projectSlide.id)
    /*def session = sessionFactory.getCurrentSession()
    session.clear()     */
    //hibSession.

    return [data : [projectSlide : projectSlide], status : 201]
  }
  //def sessionFactory

}
