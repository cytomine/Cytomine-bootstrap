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

    static def getMetadataURLWithImageId(String url,Long idImage) {
        return url + "/api/image/"+idImage+"/metadata.json"
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

    static Date getTimeAndReset(Date start,String op) {
        Date end = new Date()
        println "Time for $op = " +  (end.time - start.time)
        return end
    }
}
