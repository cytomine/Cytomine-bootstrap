package cytomine.web

import be.cytomine.image.AbstractImage

class ExtractImageMetadataJob {

    def imagePropertiesService

    static triggers = {
        simple name: 'extractImageMetadataJob', startDelay: 5000, repeatInterval: 1000*60*10
    }

    def execute() {
        Collection<AbstractImage> abstractImages = AbstractImage.findAllByWidthIsNull()
        abstractImages.each { image ->
            imagePropertiesService.extractUseful(image)
        }
    }
}
