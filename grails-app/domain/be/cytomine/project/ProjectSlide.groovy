package be.cytomine.project

import grails.converters.JSON

class ProjectSlide implements Serializable{

  Project project
  Slide slide

  String toString()
  {
    "[" + this.id + " <" + project + "," + slide + ">]"
  }

  static ProjectSlide link(Project project,Slide slide) {

    def projectSlide = ProjectSlide.findByProjectAndSlide(project, slide)
    //Project.withTransaction {
      if (!projectSlide) {
        projectSlide = new ProjectSlide()
        project?.addToProjectSlide(projectSlide)
        slide?.addToProjectSlide(projectSlide)
        println "save projectSlide"
        project.refresh()
        slide.refresh()
        projectSlide.save(flush:true)
      } else throw new IllegalArgumentException("Project " + project.id + " and slide " + slide.id + " are already mapped")
    //}
    return projectSlide
  }


  static ProjectSlide link(long id,Project project,Slide slide) {
    def projectSlide = ProjectSlide.findByProjectAndSlide(project, slide)

    if (!projectSlide) {
      projectSlide = new ProjectSlide()
      projectSlide.id = id
      project?.addToProjectSlide(projectSlide)
      slide?.addToProjectSlide(projectSlide)
        project.refresh()
        slide.refresh()
      projectSlide.save(flush:true)
    } else throw new IllegalArgumentException("Project " + project.id + " and slide " + slide.id + " are already mapped")
    return projectSlide
  }

  static void unlink(Project project, Slide slide) {
    def projectSlide = ProjectSlide.findByProjectAndSlide(project, slide)

    println "unlink projectSlide="+projectSlide
    if (projectSlide) {
        project?.removeFromProjectSlide(projectSlide)
        slide?.removeFromProjectSlide(projectSlide)
      projectSlide.delete(flush : true)

    }
  }

  static ProjectSlide createProjectSlideFromData(jsonProjectSlide) {
    def projectSlide = new ProjectSlide()
    getProjectSlideFromData(projectSlide,jsonProjectSlide)
  }

  static ProjectSlide getProjectSlideFromData(projectSlide,jsonProjectSlide) {
    println "jsonProjectSlide from getProjectSlideFromData = " + jsonProjectSlide
    projectSlide.project = Project.get(jsonProjectSlide.project.toString())
    projectSlide.slide = Slide.get(jsonProjectSlide.slide.toString())
    return projectSlide;
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + ProjectSlide.class
    JSON.registerObjectMarshaller(ProjectSlide) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['project'] = it.project?.id
      returnArray['slide'] = it.slide?.id
      return returnArray
    }
  }
}