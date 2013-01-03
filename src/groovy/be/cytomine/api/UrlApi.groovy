package be.cytomine.api

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 1/03/11
 * Time: 13:33
 * Utility class to build url for specific data.
 * Some service has special url. E.g. An annotation can be downloaded via a jpg file from url.
 */
class UrlApi {

    /**
     * Return cytomine url to get an image metadata
     * @param url Cytomine base url
     * @param idImage Image id
     * @return full cytomine url
     */
    static def getMetadataURLWithImageId(String url,Long idImage) {
        return url + "/api/image/"+idImage+"/metadata.json"
    }

    /**
     * Return cytomine url to get a user crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getUserAnnotationCropWithAnnotationId(String url,Long idAnnotation) {
        return url+'/api/userannotation/' + idAnnotation +'/crop.jpg'
    }

    /**
     * Return cytomine url to get a user crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @param maxWidthOrHeight Max size for the downloaded picture
     * @return full cytomine url
     */
    static def getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(String url, Long idAnnotation, int maxWidthOrHeight) {
        return url+'/api/userannotation/' + idAnnotation +'/crop.jpg?max_size='+maxWidthOrHeight
    }

    /**
     * Return cytomine url to get an algo crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getAlgoAnnotationCropWithAnnotationId(String url,Long idAnnotation) {
        return url+'/api/algoannotation/' + idAnnotation +'/crop.jpg'
    }

    /**
     * Return cytomine url to get a small crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getAnnotationMinCropWithAnnotationId(String url,Long idAnnotation) {
        return url+'/api/annotation/' + idAnnotation +'/cropMin.jpg'
    }

    /**
     * Return cytomine url to get an algo crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @param maxWidthOrHeight Max size for the downloaded picture
     * @return full cytomine url
     */
    static def getAlgoAnnotationCropWithAnnotationIdWithMaxWithOrHeight(String url, Long idAnnotation, int maxWidthOrHeight) {
        return url+'/api/algoannotation/' + idAnnotation +'/crop.jpg?max_size='+maxWidthOrHeight
    }

    /**
     * Return cytomine url to get an reviewed crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getReviewedAnnotationCropWithAnnotationId(String url,Long idAnnotation) {
        return url+'/api/reviewedannotation/' + idAnnotation +'/crop.jpg'
    }

    /**
     * Return cytomine url to get a reviewed crop annotation
     * @param url Cytomine base url
     * @param idAnnotation Annotation id
     * @param maxWidthOrHeight Max size for the downloaded picture
     * @return full cytomine url
     */
    static def getReviewedAnnotationCropWithAnnotationIdWithMaxWithOrHeight(String url, Long idAnnotation, int maxWidthOrHeight) {
        return url+'/api/reviewedannotation/' + idAnnotation +'/crop.jpg?max_size='+maxWidthOrHeight
    }

    /**
     * Return cytomine url to access to an annotation with the UI client
     * @param url Cytomine base url
     * @param idProject Project id
     * @param idImage Image id
     * @param idAnnotation Annotation id
     * @return full cytomine url
     */
    static def getAnnotationURL(String url,Long idProject, Long idImage, Long idAnnotation) {
        return  url + '/#tabs-image-' + idProject + "-" + idImage + "-" +  idAnnotation
    }
}
