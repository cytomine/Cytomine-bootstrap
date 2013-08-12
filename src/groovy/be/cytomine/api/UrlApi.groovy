package be.cytomine.api

import grails.util.Holders

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 1/03/11
 * Time: 13:33
 * Utility class to build url for specific data.
 * Some service has special url. E.g. An annotation can be downloaded via a jpg file from url.
 */
class UrlApi {

    def grailsApplication

    static def getApiURL(String type, Long id) {
        return "${serverUrl()}/api/$type/${id}.json"
    }

    /**
     * Return cytomine url to get an image metadata
     * @param url Cytomine base url
     * @param idImage Image id
     * @return full cytomine url
     */
    static def getMetadataURLWithImageId(Long idImage) {
        return "${serverUrl()}/api/image/$idImage/metadata.json"
    }

    /**
     * Return cytomine url to get a user crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getUserAnnotationCropWithAnnotationId(Long idAnnotation) {
        return "${serverUrl()}/api/userannotation/$idAnnotation/crop.jpg"
    }

    /**
     * Return cytomine url to get a user crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @param maxWidthOrHeight Max size for the downloaded picture
     * @return full cytomine url
     */
    static def getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(Long idAnnotation, int maxWidthOrHeight) {
        return "${serverUrl()}/api/userannotation/$idAnnotation/crop.jpg?max_size=$maxWidthOrHeight"
    }

    /**
     * Return cytomine url to get an algo crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getAlgoAnnotationCropWithAnnotationId(Long idAnnotation) {
        return "${serverUrl()}/api/algoannotation/$idAnnotation/crop.jpg"
    }

    /**
     * Return cytomine url to get a small crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getAnnotationMinCropWithAnnotationId(Long idAnnotation) {
        return "${serverUrl()}/api/annotation/$idAnnotation/cropMin.jpg"
    }

    /**
     * Return cytomine url to get an algo crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @param maxWidthOrHeight Max size for the downloaded picture
     * @return full cytomine url
     */
    static def getAlgoAnnotationCropWithAnnotationIdWithMaxWithOrHeight(Long idAnnotation, int maxWidthOrHeight) {
        return "${serverUrl()}/api/algoannotation/$idAnnotation/crop.jpg?max_size=$maxWidthOrHeight"
    }

    /**
     * Return cytomine url to get an reviewed crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getReviewedAnnotationCropWithAnnotationId(Long idAnnotation) {
        return "${serverUrl()}/api/reviewedannotation/$idAnnotation/crop.jpg"
    }

    /**
     * Return cytomine url to get a reviewed crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @param maxWidthOrHeight Max size for the downloaded picture
     * @return full cytomine url
     */
    static def getReviewedAnnotationCropWithAnnotationIdWithMaxWithOrHeight(Long idAnnotation, int maxWidthOrHeight) {
        return "${serverUrl()}/api/reviewedannotation/$idAnnotation/crop.jpg?max_size=$maxWidthOrHeight"
    }

    /**
     * Return cytomine url to access to an annotation with the UI client
     * @param url Cytomine base url
     * @param idProject Project id
     * @param idImage Image id
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getAnnotationURL(Long idProject, Long idImage, Long idAnnotation) {
        return  "${serverUrl()}/#tabs-image-$idProject-$idImage-$idAnnotation"
    }

    /**
     * Return cytomine url to access to an image with the UI client
     * @param url Cytomine base url
     * @param idProject Project id
     * @param idImage Image id
     * @return full cytomine url
     */
    static def getBrowseImageInstanceURL(Long idProject, Long idImage) {
        return  "${serverUrl()}/#tabs-image-$idProject-$idImage-"
    }

    static def getDashboardURL(Long idProject) {
        return  "${serverUrl()}/#tabs-dashboard-$idProject"
    }

    /**
     * Return cytomine url to access an image thumb
     * @param url  Cytomine base url
     * @param idImage Image id
     * @return full cytomine url
     */
    static def getAbstractImageThumbURL(Long idImage) {
        return  "${serverUrl()}/api/image/$idImage/thumb.png"
    }

    static def serverUrl() {
        Holders.getGrailsApplication().config.grails.serverURL
    }
}
