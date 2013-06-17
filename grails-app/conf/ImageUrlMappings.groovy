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
        "/api/camera"(controller: "restImage"){
            action = [POST:"camera"]
        }
        "/api/image/$id"(controller: "restImage"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/image/$id/thumb"(controller: "restImage"){
            action = [GET:"thumb"]
        }
		"/api/image/$id/preview"(controller: "restImage"){
            action = [GET:"preview"]
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
            action = [GET:"imageServers"]
        }

        "/api/project/$id/image"(controller: "restImage"){
            action = [GET:"listByProject"]
        }

        "/api/annotation/$id/$zoom/crop"(controller: "restImage"){
            action = [GET:"cropAnnotation"]
        }
        "/api/annotation/$id/crop"(controller: "restImage"){
            action = [GET:"cropAnnotation"]
        }
        "/api/annotation/$id/cropMin"(controller: "restImage"){
            action = [GET:"cropAnnotationMin"]
        }




        "/api/userannotation/$id/$zoom/crop"(controller: "restImage"){
            action = [GET:"cropUserAnnotation"]
        }
        "/api/userannotation/$id/crop"(controller: "restImage"){
            action = [GET:"cropUserAnnotation"]
        }

        "/api/algoannotation/$id/$zoom/crop"(controller: "restImage"){
            action = [GET:"cropAlgoAnnotation"]
        }
        "/api/algoannotation/$id/crop"(controller: "restImage"){
            action = [GET:"cropAlgoAnnotation"]
        }

        "/api/reviewedannotation/$id/$zoom/crop"(controller: "restImage"){
            action = [GET:"cropReviewedAnnotation"]
        }
        "/api/reviewedannotation/$id/crop"(controller: "restImage"){
            action = [GET:"cropReviewedAnnotation"]
        }

        "/api/image/$idabstractimage/group/$idgroup"(controller:"restAbstractImageGroup"){
            action = [GET:"show",DELETE:"delete",POST:"add"]
        }
    }
}


