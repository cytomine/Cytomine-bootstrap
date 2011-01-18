package be.cytomine.project

class ProjectScan {

  Project project
  Scan scan

  static ProjectScan link(Project project,Scan scan) {
    def projectScan = ProjectScan.findByProjectAndScan(project, scan)
    if (!projectScan) {
      projectScan = new ProjectScan()
      project?.addToProjectScan(projectScan)
      scan?.addToProjectScan(projectScan)
      projectScan.save()
    }
    return projectScan
  }

  static void unlink(Project project, Scan scan) {
    def projectScan = ProjectScan.findByProjectAndScan(project, scan)
    if (projectScan) {
      project?.removeFromProjectScan(projectScan)
      scan?.removeFromProjectScan(projectScan)
      projectScan.delete()
    }

  }
}
