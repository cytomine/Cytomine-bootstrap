package be.cytomine.image.server

import grails.converters.JSON
import org.apache.log4j.Logger

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


    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + ImageServerStorage.class)
        JSON.registerObjectMarshaller(ImageServerStorage) {
            def returnArray = [:]
            returnArray['imageServer'] = it.imageServer
            returnArray['storage'] = it.storage
            //we exclude credentials information (password, keys) from marshaller
            return returnArray
        }
    }
}
