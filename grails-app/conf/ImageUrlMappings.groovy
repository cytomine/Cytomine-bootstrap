/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:52
 */
class ImageUrlMappings {

    static mappings = {
        /* Abstract Image */
        "/api/image.$format"(controller: "restImage"){
            action = [GET:"list", POST:"add"]
        }
        "/api/camera.$format"(controller: "restImage"){
            action = [POST:"camera"]
        }
        "/api/image/$id.$format"(controller: "restImage"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/image/$id/thumb.$format"(controller: "restImage"){
            action = [GET:"thumb"]
        }
		"/api/image/$id/preview.$format"(controller: "restImage"){
            action = [GET:"preview"]
        }
        "/api/image/$id/metadata.$format"(controller: "restImage"){
            action = [GET:"metadata"]
        }
        "/api/image/$id/property.$format"(controller: "restImage"){
            action = [GET:"imageProperties"]
        }
        "/api/image/$id/property/$imageproperty.$format"(controller: "restImage"){
            action = [GET:"imageProperty"]
        }
        "/api/image/$id/imageservers.$format"(controller: "restImage"){
            action = [GET:"imageServers"]
        }
//        "/api/image/$id/imageserversmerge.$format"(controller: "restImage"){
//            action = [GET:"imageServers"]
//        }
//        "/api/image/$id/merge.$format"(controller: "restImage"){
//            action = [GET:"imageServers"]
//        }




        "/api/project/$id/image.$format"(controller: "restImage"){
            action = [GET:"listByProject"]
        }

        "/api/annotation/$id/$zoom/crop.$format"(controller: "restImage"){
            action = [GET:"cropAnnotation"]
        }
        "/api/annotation/$id/crop.$format"(controller: "restImage"){
            action = [GET:"cropAnnotation"]
        }
        "/api/annotation/$id/cropMin.$format"(controller: "restImage"){
            action = [GET:"cropAnnotationMin"]
        }
        "/api/userannotation/$id/$zoom/crop.$format"(controller: "restImage"){
            action = [GET:"cropUserAnnotation"]
        }
        "/api/userannotation/$id/crop.$format"(controller: "restImage"){
            action = [GET:"cropUserAnnotation"]
        }
        "/api/algoannotation/$id/$zoom/crop.$format"(controller: "restImage"){
            action = [GET:"cropAlgoAnnotation"]
        }
        "/api/algoannotation/$id/crop.$format"(controller: "restImage"){
            action = [GET:"cropAlgoAnnotation"]
        }

        "/api/reviewedannotation/$id/$zoom/crop.$format"(controller: "restImage"){
            action = [GET:"cropReviewedAnnotation"]
        }
        "/api/reviewedannotation/$id/crop.$format"(controller: "restImage"){
            action = [GET:"cropReviewedAnnotation"]
        }

        "/api/image/$idabstractimage/group/$idgroup.$format"(controller:"restAbstractImageGroup"){
            action = [GET:"show",DELETE:"delete",POST:"add"]
        }


        "/api/image/$idImage/properties/clear.$format"(controller:"restUploadedFile"){
            action = [POST:"clearProperties"]
        }
        "/api/image/$idImage/properties/populate.$format"(controller:"restUploadedFile"){
            action = [POST:"populateProperties"]
        }
        "/api/image/$idImage/properties/extract.$format"(controller:"restUploadedFile"){
            action = [POST:"extractProperties"]
        }

        "/api/uploadedfile/$uploadedFile/image.$format"(controller:"restUploadedFile"){
            action = [POST:"createImage"]
        }
    }
}


