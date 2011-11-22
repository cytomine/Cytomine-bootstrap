package be.cytomine

import be.cytomine.processing.ImageFilterProject
import be.cytomine.project.Project
import be.cytomine.processing.ImageFilter

class ImageFilterService {

    static transactional = true

    def list(Project project) {
        def imagesFiltersProject = ImageFilterProject.findAllByProject(project)
        return imagesFiltersProject.collect { it.imageFilter }
    }

    def list() {
        ImageFilter.list()
    }

    def read() {
        ImageFilter.read(params.id)
    }
}
