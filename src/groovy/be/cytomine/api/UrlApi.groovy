package be.cytomine.api

import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 1/03/11
 * Time: 13:33
 * To change this template use File | Settings | File Templates.
 */
class UrlApi {

    static def getAbstractImageURLWithProjectId(Long idProject) {
        return ConfigurationHolder.config.grails.serverURL + '/api/project/'+ idProject +'/image.json'
    }

    static def getImageInstanceURLWithProjectId(Long idProject) {
        return ConfigurationHolder.config.grails.serverURL + '/api/project/'+ idProject +'/imageinstance.json'
    }

    static def getTermsURLWithOntologyId(Long idOntology) {
        return ConfigurationHolder.config.grails.serverURL + '/api/ontology/'+ idOntology +'/term.json'
    }

    static def getTermsURLWithAnnotationId(Long idAnnotation) {
        return ConfigurationHolder.config.grails.serverURL + '/api/annotation/'+ idAnnotation +'/term.json'
    }

    static def getTermsURLWithImageId(Long idImage) {
        return ConfigurationHolder.config.grails.serverURL + '/api/image/'+ idImage +'/term.json'
    }

    static def getThumbURLWithImageId(Long idImage) {
        return ConfigurationHolder.config.grails.serverURL + "/api/image/"+idImage+"/thumb.jpg"
    }

    static def getPreviewURLWithImageId(Long idImage) {
        return ConfigurationHolder.config.grails.serverURL + "/api/image/"+idImage+"/preview.jpg"
    }

    static def getMetadataURLWithImageId(Long idImage) {
        return ConfigurationHolder.config.grails.serverURL + "/api/image/"+idImage+"/metadata.json"
    }

    static def getImageServerInfosWithImageId(Long idImage) {
        return ConfigurationHolder.config.grails.serverURL + "/api/image/"+idImage+"/imageservers.json"
    }

    static def getOntologyURLWithOntologyId(Long idOntology) {
        return ConfigurationHolder.config.grails.serverURL + '/api/ontology/'+ idOntology +'.json'
    }

    static def getUsersURLWithProjectId(Long idProject) {
        return ConfigurationHolder.config.grails.serverURL + '/api/project/'+ idProject +'/user.json'
    }

    static def getAnnotationCropWithAnnotationId(Long idAnnotation) {
        return  ConfigurationHolder.config.grails.serverURL + '/api/annotation/' + idAnnotation +'/crop.jpg'
    }

    static def getAnnotationURL(Long idProject, Long idImage, Long idAnnotation) {
        return  ConfigurationHolder.config.grails.serverURL + '/#tabs-image-' + idProject + "-" + idImage + "-" +  idAnnotation
    }
}
