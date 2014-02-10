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
        "/api/imageinstance/$id/download"(controller: "restImageInstance"){
            action = [GET:"download"]
        }
        "/api/imageinstance/$id/metadata.$format"(controller: "restImageInstance"){
            action = [GET:"metadata"]
        }
        "/api/imageinstance/$id/associated.$format"(controller: "restImageInstance"){
            action = [GET:"associated"]
        }
        "/api/imageinstance/$id/associated/$label.$format"(controller: "restImageInstance"){
            action = [GET:"label"]
        }
        "/api/imageinstance/$id/property.$format"(controller: "restImageInstance"){
            action = [GET:"imageProperties"]
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

        "/api/imageinstance/$id/copymetadata.$format"(controller :"restImageInstance") {
            action = [POST:"copyMetadata"]
        }

        "/api/imageinstance/$idImage/nested.$format"(controller: "restNestedImageInstance"){
            action = [POST:"add", GET : "listByImageInstance"]
        }
        "/api/imageinstance/$idImage/nested/$id.$format"(controller: "restNestedImageInstance"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
    }
}
