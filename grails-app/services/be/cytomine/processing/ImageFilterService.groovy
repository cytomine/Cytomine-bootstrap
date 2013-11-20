package be.cytomine.processing

import be.cytomine.SecurityACL

class ImageFilterService {

    static transactional = true
    def cytomineService

    def list() {
        SecurityACL.checkGuest(cytomineService.currentUser)
        ImageFilter.list()
    }

    def read(def id) {
        SecurityACL.checkGuest(cytomineService.currentUser)
        ImageFilter.read(id)
    }
}
