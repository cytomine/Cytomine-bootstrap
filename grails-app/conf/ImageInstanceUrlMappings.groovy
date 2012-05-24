/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageInstanceUrlMappings {

    static mappings = {
        /* Image Instance */
        "/api/imageinstance"(controller: "restImageInstance"){
            action = [POST:"add"]
        }
        "/api/imageinstance/$id"(controller: "restImageInstance"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/$id/window-$x-$y-$w-$h"(controller: "restImageInstance"){
            action = [GET:"window"]
        }
        "/api/imageinstance/$id/mask-$x-$y-$w-$h-$term"(controller: "restImageInstance"){
            action = [GET:"mask", POST : "putMask"]
        }
        "/api/annotation/$annotation/mask-$term"(controller: "restImageInstance"){
            action = [GET:"cropmask"]
        }
        "/api/project/$id/imageinstance"(controller: "restImageInstance"){
            action = [GET:"listByProject"]
        }
        "/api/project/$idproject/image/$idimage/imageinstance"(controller:"restImageInstance"){
            action = [GET:"showByProjectAndImage",DELETE:"delete"]
        }
        "/api/imageinstance/$id/cropgeometry"(controller :"restImageInstance") {
            action = [GET:"cropGeometry"]
        }
    }
}
