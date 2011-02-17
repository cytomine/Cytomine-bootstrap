package be.cytomine.project

class ProjectSlide {

  Project project
  Slide slide

  static mapping = {
    version false
  }

  static ProjectSlide link(Project project,Slide slide) {
    println "Link Project " + project + " with slide " + slide
    def projectSlide = ProjectSlide.findByProjectAndSlide(project, slide)
    if (!projectSlide) {
      println "LINKED"
      projectSlide = new ProjectSlide()
      project?.addToProjectSlide(projectSlide)
      slide?.addToProjectSlide(projectSlide)
      projectSlide.save(flush : true)
    }
    return projectSlide
  }

  static void unlink(Project project, Slide slide) {
    def projectSlide = ProjectSlide.findByProjectAndSlide(project, slide)
    if (projectSlide) {
      project?.addToProjectSlide(projectSlide)
      slide?.addToProjectSlide(projectSlide)
      projectSlide.delete(flush : true)
    }

  }
}
