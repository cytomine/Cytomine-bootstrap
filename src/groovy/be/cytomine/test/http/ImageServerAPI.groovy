package be.cytomine.test.http

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.test.Infos

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AbstractImage to Cytomine with HTTP request during functional test
 */
class ImageServerAPI extends DomainAPI {

    static def thumb(Long idImage,String username, String password, int maxSize = 256) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage/$idImage/thumb.jpg?maxSize=$maxSize"
        return downloadImage(URL,username,password)
    }

    static def preview(Long idImage,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage/$idImage/preview.png"
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
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/crop.json?zoom=$zoom"
        return downloadImage(URL,username,password)
    }

    static def cropAnnotation(Long idAnnotation,Boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&maxSize=$maxSize":"" )
        return downloadImage(URL,username,password)
    }

    static def cropAnnotationMin(Long idAnnotation,Boolean draw, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/crop.jpg?maxSize=256"  + (draw?"&draw=true":"" )
        return downloadImage(URL,username,password)
    }

    static def cropUserAnnotation(Long idAnnotation,Boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/$idAnnotation/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&maxSize=$maxSize":"" )
        return downloadImage(URL,username,password)
    }
    static def cropUserAnnotation(Long idAnnotation,int zoom,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/$idAnnotation/crop.jpg?zoom=$zoom"  + (draw?"&draw=true":"" ) + (maxSize?"&maxSize=$maxSize":"" )
        return downloadImage(URL,username,password)
    }

    static def cropAlgoAnnotation(Long idAnnotation,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/$idAnnotation/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&maxSize=$maxSize":"" )
        return downloadImage(URL,username,password)
    }
    static def cropAlgoAnnotation(Long idAnnotation,int zoom,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/$idAnnotation/crop.jpg?zoom=$zoom"  + (draw?"&draw=true":"" ) + (maxSize?"&maxSize=$maxSize":"" )
        return downloadImage(URL,username,password)
    }

    static def cropReviewedAnnotation(Long idAnnotation,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/$idAnnotation/crop.jpg?"  + (draw?"&draw=true":"" ) + (maxSize?"&maxSize=$maxSize":"" )
        return downloadImage(URL,username,password)
    }
    static def cropReviewedAnnotation(Long idAnnotation,int zoom,boolean draw,Integer maxSize, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/$idAnnotation/crop.jpg?zoom=$zoom"  + (draw?"&draw=true":"" ) + (maxSize?"&maxSize=$maxSize":"" )
        return downloadImage(URL,username,password)
    }

    static def imageServers(Long idImage, Long idImageInstance, Boolean merge, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/abstractimage/$idImage/imageservers.json?"  + (idImageInstance?"&imageinstance=$idImageInstance":"" ) + (merge?"&merge=true&channels=1,3&colors=ff0000,00ff00":"" )
        return doGET(URL,username,password)
    }

    static def mask(Long idImageInstance, int x, int y, int w, int h, Long idTerm, Long idUser, def idAnnotations, String username, String password) {
        if (idAnnotations) idAnnotations = idAnnotations.join(',') //join array [id1, id2] to a String 'id1, id2'
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImageInstance/window-$x-$y-$w-$h" + ".png?mask=true"  + (idAnnotations?"&annotations=$idAnnotations":"" ) + (idTerm?"&terms=$idTerm":"" ) + (idUser?"&users=$idUser":"" )
        return downloadImage(URL,username,password)
    }

    static def maskReviewed(Long idImageInstance, int x, int y, int w, int h, Long idTerm, Long idUser, def idAnnotations, String username, String password) {
        if (idAnnotations) idAnnotations = idAnnotations.join(',') //join array [id1, id2] to a String 'id1, id2'
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$idImageInstance/window-$x-$y-$w-$h" + ".png?mask=true&reviewed=true&"  + (idAnnotations?"&annotations=$idAnnotations":"" ) + (idTerm?"&terms=$idTerm":"" ) + (idUser?"&users=$idUser":"" )
        return downloadImage(URL,username,password)
    }

    static def maskUserAnnotation(Long idAnnotation,  String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/$idAnnotation/crop.jpg&mask=true"
        return downloadImage(URL,username,password)
    }

    static def maskUserAnnotationAlpha(Long idAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/$idAnnotation/crop.png&alphaMask=true"
        return downloadImage(URL,username,password)
    }

    static def maskAlgoAnnotationAlpha(Long idAnnotation,  String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/$idAnnotation/crop.png&alphaMask=true"
        return downloadImage(URL,username,password)
    }

    static def maskReviewedAnnotationAlpha(Long idAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/$idAnnotation/crop.png&alphaMask=true"
        return downloadImage(URL,username,password)
    }

    static def listImageServerByMime(String mimeType, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageserver.json?mimeType=$mimeType"
        return doGET(URL,username,password)
    }

//    "/api/userannotation/$annotation/mask-$term"(controller: "restImageInstance"){
//        action = [GET:"cropmask"]
//    }
//    "/api/userannotation/$annotation/alphamask-$term"(controller: "restImageInstance"){
//        action = [GET:"alphamaskUserAnnotation"]
//    }
//    "/api/algoannotation/$annotation/alphamask-$term"(controller: "restImageInstance"){
//        action = [GET:"alphamaskAlgoAnnotation"]
//    }
//    "/api/reviewedannotation/$annotation/alphamask-$term"(controller: "restImageInstance"){
//        action = [GET:"alphamaskReviewedAnnotation"]
//    }
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
