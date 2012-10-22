package be.cytomine.api

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 1/03/11
 * Time: 13:33
 * Utility class to build url for specific data
 */
class UrlApi {

    static def getMetadataURLWithImageId(String url,Long idImage) {
        return url + "/api/image/"+idImage+"/metadata.json"
    }

    static def getThumbURLWithImageId(String url,Long idImage) {
        return url + "/api/image/"+idImage+"/thumb.png"
    }

    static def getPreviewURLWithImageId(String url, Long idImage) {
        return url + "/api/image/"+idImage+"/preview.png"
    }

    static def getUserAnnotationCropWithAnnotationId(String url,Long idAnnotation) {
        return url+'/api/userannotation/' + idAnnotation +'/crop.jpg'
    }

    static def getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(String url, Long idAnnotation, int maxWidthOrHeight) {
        return url+'/api/userannotation/' + idAnnotation +'/crop.jpg?max_size='+maxWidthOrHeight
    }

    static def getAlgoAnnotationCropWithAnnotationId(String url,Long idAnnotation) {
        return url+'/api/algoannotation/' + idAnnotation +'/crop.jpg'
    }

    static def getAlgoAnnotationCropWithAnnotationIdWithMaxWithOrHeight(String url, Long idAnnotation, int maxWidthOrHeight) {
        return url+'/api/algoannotation/' + idAnnotation +'/crop.jpg?max_size='+maxWidthOrHeight
    }

    static def getAnnotationURL(String url,Long idProject, Long idImage, Long idAnnotation) {
        return  url + '/#tabs-image-' + idProject + "-" + idImage + "-" +  idAnnotation
    }
}
