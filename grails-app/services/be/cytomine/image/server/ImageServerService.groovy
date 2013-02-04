package be.cytomine.image.server

import be.cytomine.utils.ModelService

class ImageServerService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService

    //TODO: move here code for Image Server CRUD

}
