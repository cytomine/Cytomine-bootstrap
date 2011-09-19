package cytomine.web

import be.cytomine.image.AbstractImage

class ExtractImageMetadataJob {

    def imagePropertiesService

    def startDelay = 5000 //start 5 seconds after server is running
    def timeout = 1000*60*10 //each 10 minutes

    def execute() {
        Collection<AbstractImage> abstractImages = AbstractImage.findAllByWidthIsNull()
        abstractImages.each { image ->
            imagePropertiesService.extractUseful(image)
        }
    }
}
