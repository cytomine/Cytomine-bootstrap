/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageInstanceUrlMappings {

    static mappings = {
        /* Image Instance */
        "/api/imageinstance.$format"(controller: "restImageInstance"){
            action = [POST:"add"]
        }
        "/api/imageinstance/$id.$format"(controller: "restImageInstance"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/$id/next.$format"(controller: "restImageInstance"){
            action = [GET:"next"]
        }
        "/api/user/$user/imageinstance/light.$format"(controller: "restImageInstance"){
            action = [GET:"listByUser"]
        }
        "/api/imageinstance/$id/previous.$format"(controller: "restImageInstance"){
            action = [GET:"previous"]
        }
        "/api/imageinstance/$id/window-$x-$y-$w-$h.$format"(controller: "restImageInstance"){
            action = [GET:"window"]
        }
        "/api/imageinstance/$id/window_url-$x-$y-$w-$h.$format"(controller: "restImageInstance"){
            action = [GET:"windowUrl"]
        }
        "/api/imageinstance/$id/mask.$format"(controller: "restImageInstance"){
            action = [GET:"mask", POST : "putMask"]
        }
        "/api/userannotation/$annotation/mask-$term.$format"(controller: "restImageInstance"){
            action = [GET:"cropmask"]
        }
        "/api/userannotation/$annotation/alphamask-$term.$format"(controller: "restImageInstance"){
            action = [GET:"alphamaskUserAnnotation"]
        }
        "/api/algoannotation/$annotation/alphamask-$term.$format"(controller: "restImageInstance"){
            action = [GET:"alphamaskAlgoAnnotation"]
        }
        "/api/reviewedannotation/$annotation/alphamask-$term.$format"(controller: "restImageInstance"){
            action = [GET:"alphamaskReviewedAnnotation"]
        }
        "/api/project/$id/imageinstance.$format"(controller: "restImageInstance"){
            action = [GET:"listByProject"]
        }
        "/api/imageinstance/$id/cropgeometry.$format"(controller :"restImageInstance") {
            action = [GET:"cropGeometry"]
        }
        "/api/imageinstance/method/lastopened.$format"(controller :"restImageInstance") {
            action = [GET:"listLastOpenImage"]
        }

//        "/api/imageinstance/lastopened.$format"(controller :"restImageInstance") {
//            action = [GET:"listLastOpenImage"]
//        }
        "/api/imageinstance/$id/sameimagedata.$format"(controller :"restImageInstance") {
            action = [GET:"retrieveSameImageOtherProject"]
        }
        "/api/imageinstance/$id/copyimagedata.$format"(controller :"restImageInstance") {
            action = [POST:"copyAnnotationFromSameAbstractImage"]
        }
    }
}
