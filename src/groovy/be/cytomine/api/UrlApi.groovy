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

    static def getCropURL(Long idImage, def parameters) {
        String url = "${serverUrl()}/api/abstractimage/$idImage/crop.png"
        String query = parameters.collect { key, value ->
            if (value instanceof String)
                value = URLEncoder.encode(value, "UTF-8")
            "$key=$value"
        }.join("&")
        return "$url?$query"
    }

    /**
     * Return cytomine url to get an image metadata
     * @param url Cytomine base url
     * @param idImage Image id
     * @return full cytomine url
     */
    static def getMetadataURLWithImageId(Long idImage) {
        return "${serverUrl()}/api/abstractimage/$idImage/metadata.json"
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
     * Return cytomine url to get a roi crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getROIAnnotationCropWithAnnotationId(Long idAnnotation) {
        return "${serverUrl()}/api/roiannotation/$idAnnotation/crop.jpg"
    }

    /**
     * Return cytomine url to get a user crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @param maxWidthOrHeight Max size for the downloaded picture
     * @return full cytomine url
     */
    static def getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(Long idAnnotation, int maxWidthOrHeight) {
        return "${serverUrl()}/api/userannotation/$idAnnotation/crop.png?maxSize=$maxWidthOrHeight"
    }

    /**
     * Return cytomine url to get an algo crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getAlgoAnnotationCropWithAnnotationId(Long idAnnotation) {
        return "${serverUrl()}/api/algoannotation/$idAnnotation/crop.png"
    }

    static def getAnnotationCropWithAnnotationId(Long idAnnotation) {
        return "${serverUrl()}/api/algoannotation/$idAnnotation/crop.png"
    }

    static def getAssociatedImage(Long idAbstractImage, String label, def maxSize) {
        return "${serverUrl()}/api/abstractimage/$idAbstractImage/associated/$label" + ".png?maxWidth=$maxSize"
    }

    static def getThumbImage(Long idAbstractImage, def maxSize) {
        return "${serverUrl()}/api/abstractimage/$idAbstractImage/thumb.png?maxWidth=$maxSize"
    }

    /**
     * Return cytomine url to get a small crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getAnnotationMinCropWithAnnotationId(Long idAnnotation) {
        return "${serverUrl()}/api/annotation/$idAnnotation/crop.png?maxSize=256"
    }
    static def getAnnotationMinCropWithAnnotationIdOld(Long idAnnotation) {
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
        return "${serverUrl()}/api/algoannotation/$idAnnotation/crop.png?maxSize=$maxWidthOrHeight"
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
        return "${serverUrl()}/api/reviewedannotation/$idAnnotation/crop.jpg?maxSize=$maxWidthOrHeight"
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
        return  "${serverUrl()}/api/abstractimage/$idImage/thumb.png"
    }

    static def serverUrl() {
        Holders.getGrailsApplication().config.grails.serverURL
    }
}
