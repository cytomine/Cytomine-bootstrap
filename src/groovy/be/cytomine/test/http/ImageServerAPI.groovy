package be.cytomine.test.http

import be.cytomine.test.Infos

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AbstractImage to Cytomine with HTTP request during functional test
 */
class ImageServerAPI extends DomainAPI {

    static def thumb(Long idImage,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/$idImage/thumb.jpg"
        return downloadImage(URL,username,password)
    }

    static def preview(Long idImage,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/$idImage/preview.png"
        return downloadImage(URL,username,password)
    }

    static def windowUrl(Long idImageInstance, int x, int y, int w, int h, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImageInstance/window_url-$x-$y-$w-$h" + ".jpg"
        return doGET(URL,username,password)
    }

    static def window(Long idImageInstance, int x, int y, int w, int h, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImageInstance/window-$x-$y-$w-$h" + ".jpg"
        return downloadImage(URL,username,password)
    }

    static def cropGeometry(Long idImageInstance,String geometry, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImageInstance/cropgeometry.jpg?geometry="+URLEncoder.encode(geometry, "UTF-8")
        return downloadImage(URL,username,password)
    }


    static def cropGeometryZoom(Long idAnnotation, int zoom,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/$zoom/crop.json"
        return downloadImage(URL,username,password)
    }

    static def cropAnnotation(Long idAnnotation,Boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&max_size=$maxSize":"" )
        return downloadImage(URL,username,password)
    }

    static def cropAnnotationMin(Long idAnnotation,Boolean draw, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/cropMin.jpg?"  + (draw?"&draw=true":"" )
        return downloadImage(URL,username,password)
    }

    static def cropUserAnnotation(Long idAnnotation,Boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/$idAnnotation/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&max_size=$maxSize":"" )
        return downloadImage(URL,username,password)
    }
    static def cropUserAnnotation(Long idAnnotation,int zoom,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/$idAnnotation/$zoom/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&max_size=$maxSize":"" )
        return downloadImage(URL,username,password)
    }

    static def cropAlgoAnnotation(Long idAnnotation,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/$idAnnotation/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&max_size=$maxSize":"" )
        return downloadImage(URL,username,password)
    }
    static def cropAlgoAnnotation(Long idAnnotation,int zoom,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/$idAnnotation/$zoom/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&max_size=$maxSize":"" )
        return downloadImage(URL,username,password)
    }

    static def cropReviewedAnnotation(Long idAnnotation,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/$idAnnotation/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&max_size=$maxSize":"" )
        return downloadImage(URL,username,password)
    }
    static def cropReviewedAnnotation(Long idAnnotation,int zoom,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/$idAnnotation/$zoom/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&max_size=$maxSize":"" )
        return downloadImage(URL,username,password)
    }

    static def imageServers(Long idImage, Long idImageInstance, Boolean merge, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/$idImage/imageservers?"  + (idImageInstance?"&imageinstance=$idImageInstance":"" ) + (merge?"&merge=true":"" )
        return doGET(URL,username,password)
    }



//    "/api/annotation/$id/crop"(controller: "restImage"){
//        action = [GET:"cropAnnotation"]
//    }
//    "/api/annotation/$id/cropMin"(controller: "restImage"){
//        action = [GET:"cropAnnotationMin"]
//    }
//    "/api/userannotation/$id/$zoom/crop"(controller: "restImage"){
//        action = [GET:"cropUserAnnotation"]
//    }
//    "/api/userannotation/$id/crop"(controller: "restImage"){
//        action = [GET:"cropUserAnnotation"]
//    }
//    "/api/algoannotation/$id/$zoom/crop"(controller: "restImage"){
//        action = [GET:"cropAlgoAnnotation"]
//    }
//    "/api/algoannotation/$id/crop"(controller: "restImage"){
//        action = [GET:"cropAlgoAnnotation"]
//    }
//
//    "/api/reviewedannotation/$id/$zoom/crop"(controller: "restImage"){
//        action = [GET:"cropReviewedAnnotation"]
//    }
//    "/api/reviewedannotation/$id/crop"(controller: "restImage"){
//        action = [GET:"cropReviewedAnnotation"]
//    }


//    static def clearAbstractImageProperties(Long idImage,String username, String password) {
//        return doPOST("/api/image/"+idImage+"/properties/clear.json","",username,password)
//    }
//    static def populateAbstractImageProperties(Long idImage,String username, String password) {
//        return doPOST("/api/image/"+idImage+"/properties/populate.json","",username,password)
//    }
//    static def extractUsefulAbstractImageProperties(Long idImage,String username, String password) {
//        return doPOST("/api/image/"+idImage+"/properties/extract.json","",username,password)
//    }

}
