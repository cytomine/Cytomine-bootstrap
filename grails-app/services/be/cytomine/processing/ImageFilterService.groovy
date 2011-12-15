package be.cytomine.processing

import be.cytomine.project.Project
import org.springframework.security.access.prepost.PreAuthorize

class ImageFilterService {

    static transactional = true

    @PreAuthorize("hasPermission(#project,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        def imagesFiltersProject = ImageFilterProject.findAllByProject(project)
        return imagesFiltersProject.collect { it.imageFilter }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def list() {
        ImageFilter.list()
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def read(def id) {
        ImageFilter.read(id)
    }
}
