package be.cytomine.processing



class ImageFilterService {

    static transactional = true
    def cytomineService
    def securityACLService

    def list() {
        securityACLService.checkGuest(cytomineService.currentUser)
        ImageFilter.list()
    }

    def read(def id) {
        securityACLService.checkGuest(cytomineService.currentUser)
        ImageFilter.read(id)
    }
}
