package be.cytomine.command.projectslide

import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.project.Slide
import be.cytomine.project.ProjectSlide

class DeleteProjectSlideCommand extends DeleteCommand implements UndoRedoCommand {

  def execute() {
    def postData = JSON.parse(postData)


      Project project = Project.get(postData.project)
      Slide slide = Slide.get(postData.slide)

    log.info "execute with project=" + project + " slide=" + slide
    ProjectSlide projectSlide = ProjectSlide.findByProjectAndSlide(project,slide)
    data = projectSlide.encodeAsJSON()

    if (!projectSlide) {
      return [data : [success : false, message : "ProjectSlide not found with id: " + postData.id], status : 404]
    }
    log.info "Unlink=" + projectSlide.project +" " + projectSlide.slide
    ProjectSlide.unlink(projectSlide.project, projectSlide.slide)

    return [data : [success : true, message : "OK", data : [projectSlide : postData.id]], status : 200]
  }

  def undo() {


    def projectSlideData = JSON.parse(data)
    ProjectSlide projectSlide = ProjectSlide.createProjectSlideFromData(projectSlideData)
    projectSlide = ProjectSlide.link(projectSlideData.id,projectSlide.project, projectSlide.slide)
    //projectSlide.save(flush:true)

    //save new id of the object that has been re-created
    def postDataLocal = JSON.parse(postData)
    postDataLocal.id =  projectSlide.id
    postData = postDataLocal.toString()

    log.debug "ProjectSlide with id " + projectSlide.id

    return [data : [success : true, projectSlide : projectSlide, message : "OK"], status : 201]
  }

  def redo() {
    def postData = JSON.parse(postData)

      Project project = Project.get(postData.project)
      Slide slide = Slide.get(postData.slide)
    ProjectSlide projectSlide = ProjectSlide.findByProjectAndSlide(project,slide)
    ProjectSlide.unlink(projectSlide.project, projectSlide.slide)
    return [data : [success : true, message : "OK"], status : 200]

  }

}