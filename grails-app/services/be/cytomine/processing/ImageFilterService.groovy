package be.cytomine.processing

import be.cytomine.SecurityACL
import be.cytomine.project.Project

import static org.springframework.security.acls.domain.BasePermission.READ

class ImageFilterService {

    static transactional = true
    def cytomineService

    def list() {
        SecurityACL.checkUser(cytomineService.currentUser)
        ImageFilter.list()
    }

    def read(def id) {
        SecurityACL.checkUser(cytomineService.currentUser)
        ImageFilter.read(id)
    }
}
