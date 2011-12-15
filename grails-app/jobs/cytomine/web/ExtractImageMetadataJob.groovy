package cytomine.web

import be.cytomine.image.AbstractImage

class ExtractImageMetadataJob {

    def imagePropertiesService

    static triggers = {
        simple name: 'extractImageMetadataJob', startDelay: 5000, repeatInterval: 1000*60*100
    }

    def execute() {
        println "ExtractImageMetadataJob"
        /*Collection<AbstractImage> abstractImages = AbstractImage.findAllByWidthIsNull()
        abstractImages.each { image ->
            println "Extracting... " + image
            imagePropertiesService.extractUseful(image)
        }*/
    }
}
