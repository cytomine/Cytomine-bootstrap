package be.cytomine.processing

import be.cytomine.project.Project

class ImageFilterProjectService {

    static transactional = true

    def get(Project project, ImageFilter image) {
        return ImageFilterProject.findByImageFilterAndProject(image, project)
    }

    def list(Project project) {
        return ImageFilterProject.findAllByProject(project)
    }

    def add(Project project, ImageFilter imageFilter) {
        ImageFilterProject.link(imageFilter, project)
    }

    def delete(Project project, ImageFilter imageFilter) {
        ImageFilterProject.unlink(imageFilter, project)
    }
}
