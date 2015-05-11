package be.cytomine.image

import be.cytomine.ontology.Property
import grails.converters.JSON

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
        Property.findAllByDomainIdent(image.id)?.each {
            it.delete()
        }
    }

    def populate(AbstractImage abstractImage) {
        String imageServerURL = abstractImage.getRandomImageServerURL()
        UploadedFile uploadedFile = abstractImageService.getMainUploadedFile(abstractImage)
        String fif = uploadedFile.absolutePath
        fif = fif.replace(" ","%20")
        String mimeType = uploadedFile.mimeType
        String uri = "$imageServerURL/image/properties?fif=$fif&mimeType=$mimeType"
        println uri
        def properties = JSON.parse(new URL(uri).text)
        println properties
        properties.each {
            String value = it.value
            if (value.size() < 256) {
                def property = new Property(key: it.key, value: it.value, domainIdent: abstractImage.id,domainClassName: abstractImage.class.name)
                log.info("new property, $it.key => $it.value")
                property.save()
            }

        }
        abstractImage.save()
    }


    def extractUseful(AbstractImage image) {
        def magnificationProperty = Property.findByDomainIdentAndKey(image.id, "cytomine.magnification")
        if (magnificationProperty) image.setMagnification(Integer.parseInt(magnificationProperty.getValue()))
        else log.info "magnificationProperty is null"
        //Width
        def widthProperty = Property.findByDomainIdentAndKey(image.id, "cytomine.width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        else log.info "widthProperty is null"
        //Height
        def heightProperty = Property.findByDomainIdentAndKey(image.id, "cytomine.height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        else log.info "heightProperty is null"
        //Resolution
        def resolutionProperty = Property.findByDomainIdentAndKey(image.id, "cytomine.resolution")
        if (resolutionProperty) image.setResolution(Float.parseFloat(resolutionProperty.getValue()))
        else log.info "resolutionProperty is null"
        image.save(flush:true, failOnError: true)
    }
}
