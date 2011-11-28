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
            action = [GET:"list", POST:"add"]
        }
        "/api/imageinstance/$id"(controller: "restImageInstance"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/$id/window-$x-$y-$w-$h"(controller: "restImageInstance"){
            action = [GET:"window"]
        }
        "/api/imageinstance/$id/mask-$x-$y-$w-$h-$term"(controller: "restImageInstance"){
            action = [GET:"mask"]
        }
        "/api/imageinstance/$id/mask-$annotation-$term"(controller: "restImageInstance"){
            action = [GET:"cropmask"]
        }
    }
}
