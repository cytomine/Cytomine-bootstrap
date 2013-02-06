package be.cytomine.image.server

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 5/02/13
 * Time: 11:40
 */
class ImageServerStorage {
    ImageServer imageServer
    Storage storage

    def getZoomifyUrl() {
        return imageServer.url + imageServer.service + "?zoomify=" + storage.getBasePath()
    }
}
