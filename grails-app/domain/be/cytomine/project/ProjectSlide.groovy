package be.cytomine.project

class ProjectSlide {

  Project project
  Slide slide

  static ProjectSlide link(Project project,Slide slide) {
    def projectSlide = ProjectSlide.findByProjectAndSlide(project, slide)
    if (!projectSlide) {
      projectSlide = new ProjectSlide()
      project.addToProjectSlide(projectSlide)
      slide?.addToProjectSlide(projectSlide)
      projectSlide.save()
    }
    return projectSlide
  }

  static void unlink(Project project, Slide slide) {
    def projectSlide = ProjectSlide.findByProjectAndSlide(project, slide)
    if (projectSlide) {
      project?.addToProjectSlide(projectSlide)
      slide?.addToProjectSlide(projectSlide)
      projectSlide.delete()
    }

  }
}
