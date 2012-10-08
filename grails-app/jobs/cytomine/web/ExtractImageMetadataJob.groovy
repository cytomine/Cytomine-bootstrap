package cytomine.web

import be.cytomine.image.AbstractImage

class ExtractImageMetadataJob {

    def imagePropertiesService

    static triggers = {
        //simple name: 'extractImageMetadataJob', startDelay: 60000, repeatInterval: 1000*60
    }

    def execute() {
        Collection<AbstractImage> abstractImages = AbstractImage.findAllByWidthIsNullOrWidthLike(-1)
        abstractImages.each { image ->
            imagePropertiesService.extractUseful(image)
        }
    }
}
