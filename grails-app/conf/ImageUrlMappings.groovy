/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:52
 */
class ImageUrlMappings {

    static mappings = {
        /* Abstract Image */
        "/api/abstractimage.$format"(controller: "restImage"){
            action = [GET:"list", POST:"add"]
        }
        "/api/camera.$format"(controller: "restImage"){
            action = [POST:"camera"]
        }
        "/api/abstractimage/$id.$format"(controller: "restImage"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/abstractimage/$id/thumb.$format"(controller: "restImage"){
            action = [GET:"thumb"]
        }
		"/api/abstractimage/$id/preview.$format"(controller: "restImage"){
            action = [GET:"preview"]
        }
        "/api/abstractimage/$id/metadata.$format"(controller: "restImage"){
            action = [GET:"metadata"]
        }
        "/api/abstractimage/$id/associated.$format"(controller: "restImage"){
            action = [GET:"associated"]
        }
        "/api/abstractimage/$id/associated/$label.$format"(controller: "restImage"){
            action = [GET:"label"]
        }
        "/api/abstractimage/$id/property.$format"(controller: "restImage"){
            action = [GET:"imageProperties"]
        }
        "/api/abstractimage/$id/property/$imageproperty.$format"(controller: "restImage"){
            action = [GET:"imageProperty"]
        }
        "/api/abstractimage/$id/imageservers.$format"(controller: "restImage"){
            action = [GET:"imageServers"]
        }
//        "/api/abstractimage/$id/imageserversmerge.$format"(controller: "restImage"){
//            action = [GET:"imageServers"]
//        }
//        "/api/abstractimage/$id/merge.$format"(controller: "restImage"){
//            action = [GET:"imageServers"]
//        }




        "/api/project/$id/image.$format"(controller: "restImage"){
            action = [GET:"listByProject"]
        }

        "/api/abstractimage/$id/properties/clear.$format"(controller:"restUploadedFile"){
            action = [POST:"clearProperties"]
        }
        "/api/abstractimage/$id/properties/populate.$format"(controller:"restUploadedFile"){
            action = [POST:"populateProperties"]
        }
        "/api/abstractimage/$id/properties/extract.$format"(controller:"restUploadedFile"){
            action = [POST:"extractProperties"]
        }

        "/api/uploadedfile/$uploadedFile/image.$format"(controller:"restUploadedFile"){
            action = [POST:"createImage"]
        }
    }
}


