package be.cytomine.image

import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServerStorage
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.server.resolvers.Resolver
import grails.converters.JSON
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 19/07/11
 * Time: 15:19
 * TODOSTEVBEN: doc + clean + refactor
 */
class ImagePropertiesService implements Serializable{

    def grailsApplication
    def abstractImageService

    def clear(AbstractImage image) {
        ImageProperty.findAllByImage(image)?.each {
            it.delete()
        }
    }

    def populate(AbstractImage abstractImage) {
        String imageServerURL = grailsApplication.config.grails.imageServerURL
        String fif = abstractImageService.getMainUploadedFile(abstractImage).absolutePath
        String uri = "$imageServerURL/image/properties?fif=$fif"
        println uri
        def properties = JSON.parse(new URL(uri).text)
        properties.each {
            println properties
            def property = new ImageProperty(key: it.key, value: it.value, image: abstractImage)
            log.info("new property, $it.key => $it.value")
            property.save()
        }
        abstractImage.save(flush:true, failOnError: true)
    }


    def extractUseful(AbstractImage image) {
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "cytomine.magnification")
        if (magnificationProperty) image.setMagnification(Integer.parseInt(magnificationProperty.getValue()))
        else log.info "magnificationProperty is null"
        //Width
        def widthProperty = ImageProperty.findByImageAndKey(image, "cytomine.width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        else log.info "widthProperty is null"
        //Height
        def heightProperty = ImageProperty.findByImageAndKey(image, "cytomine.height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        else log.info "heightProperty is null"
        //Resolution
        def resolutionProperty = ImageProperty.findByImageAndKey(image, "cytomine.resolution")
        if (resolutionProperty) image.setResolution(Float.parseFloat(resolutionProperty.getValue()))
        else log.info "resolutionProperty is null"
        image.save(flush:true, failOnError: true)
    }
}
