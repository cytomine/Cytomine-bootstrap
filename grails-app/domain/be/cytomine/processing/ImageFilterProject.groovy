package be.cytomine.processing

import be.cytomine.project.Project

class ImageFilterProject {

    ImageFilter imageFilter
    Project project

    static mapping = {
        version false
    }

    static ImageFilterProject link(ImageFilter imageFilter, Project user) {
        def imageFilterProject = ImageFilterProject.findByImageFilterAndProject(imageFilter, user)
        if (!imageFilterProject) {
            imageFilterProject = new SoftwareProjects()
            imageFilter?.addToImageFilterProjects(imageFilterProject)
            user?.addToImageFilterProjects(imageFilterProject)
            imageFilterProject.save(flush : true)
        }
    }

    static void unlink(ImageFilter imageFilter, Project project) {
        def imageFilterProject = ImageFilterProject.findByImageFilterAndProject(imageFilter, project)
        if (imageFilterProject) {
            imageFilter?.removeFromImageFilterProjects(imageFilterProject)
            project?.removeFromImageFilterProjects(imageFilterProject)
            imageFilterProject.delete(flush : true)
        } else {println "no link between "+imageFilter + " " + project}
    }
}

