package be.cytomine.processing

import be.cytomine.project.Project

class ImageFilterService {

    static transactional = true

    def list(Project project) {
        def imagesFiltersProject = ImageFilterProject.findAllByProject(project)
        return imagesFiltersProject.collect { it.imageFilter }
    }

    def list() {
        ImageFilter.list()
    }

    def read(def id) {
        ImageFilter.read(id)
    }
}
