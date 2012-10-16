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

    static def getAnnotationCropWithAnnotationId(String url,Long idAnnotation) {
        return url+'/api/annotation/' + idAnnotation +'/crop.jpg'
    }

    static def getAnnotationCropWithAnnotationIdWithMaxWithOrHeight(String url, Long idAnnotation, int maxWidthOrHeight) {
        return url+'/api/annotation/' + idAnnotation +'/crop.jpg?max_size='+maxWidthOrHeight
    }

    static def getAnnotationURL(String url,Long idProject, Long idImage, Long idAnnotation) {
        return  url + '/#tabs-image-' + idProject + "-" + idImage + "-" +  idAnnotation
    }
}
