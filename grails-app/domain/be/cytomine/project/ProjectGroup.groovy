package be.cytomine.project

import be.cytomine.security.Group

class ProjectGroup {

    Project project
    Group group

    static mapping = {
        version false
    }

    static ProjectGroup link(Project project, Group group) {
        def projectGroup = ProjectGroup.findByProjectAndGroup(project, group)
        if (!projectGroup) {
            projectGroup = new ProjectGroup()
            project?.addToProjectGroup(projectGroup)
            group?.addToProjectGroup(projectGroup)
            projectGroup.save(flush:true)
        }
        return projectGroup
    }

    static void unlink(Project project, Group group) {
        def projectGroup = ProjectGroup.findByProjectAndGroup(project, group)
        if (projectGroup) {
            project?.removeFromProjectGroup(projectGroup)
            group?.removeFromProjectGroup(projectGroup)
            projectGroup.delete(flush:true)
        }

    }
}
