/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:52
 */
class ImageUrlMappings {

    static mappings = {
        /* Abstract Image */
        "/api/image"(controller: "restImage"){
            action = [GET:"list", POST:"add"]
        }
        "/api/image/$id"(controller: "restImage"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/image/$id/thumb"(controller: "restImage"){
            action = [GET:"thumb"]
        }
        "/api/image/$id/metadata"(controller: "restImage"){
            action = [GET:"metadata"]
        }
        "/api/image/$id/property"(controller: "restImage"){
            action = [GET:"imageProperties"]
        }
        "/api/image/$id/property/$imageproperty"(controller: "restImage"){
            action = [GET:"imageProperty"]
        }
        "/api/image/$id/imageservers"(controller: "restImage"){
            action = [GET:"imageservers"]
        }
    }
}


