package be.cytomine.processing

import be.cytomine.security.UserGroup
import be.cytomine.project.Project

class SoftwareProjects {

    Software software
    Project project

    static mapping = {
        version false
    }

    static SoftwareProjects link(Software software, Project project) {
        def softwareProjects = SoftwareProjects.findBySoftwareAndProject(software, project)
        if (!softwareProjects) {
            softwareProjects = new SoftwareProjects()
            software?.addToSoftwareProjects(softwareProjects)
            project?.addToSoftwareProjects(softwareProjects)
            softwareProjects.save(flush : true)
        }
    }

    static void unlink(Software software, Project project) {
        def softwareProjects = SoftwareProjects.findBySoftwareAndProject(software, project)
        if (softwareProjects) {
            software?.removeFromSoftwareProjects(softwareProjects)
            project?.removeFromSoftwareProjects(softwareProjects)
            softwareProjects.delete(flush : true)
        } else {println "no link between "+software + " " + project}
    }
}
