/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:52
 */
class AbstractImageUrlMappings {

    static mappings = {
        /* Abstract Image */
        "/api/abstractimage.$format"(controller: "restAbstractImage"){
            action = [GET:"list", POST:"add"]
        }
        "/api/camera.$format"(controller: "restAbstractImage"){
            action = [POST:"camera"]
        }
        "/api/abstractimage/$id.$format"(controller: "restAbstractImage"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/abstractimage/$id/download"(controller: "restAbstractImage"){
            action = [GET:"download"]
        }
        "/api/abstractimage/$id/thumb.$format"(controller: "restAbstractImage"){
            action = [GET:"thumb"]
        }
		"/api/abstractimage/$id/preview.$format"(controller: "restAbstractImage"){
            action = [GET:"preview"]
        }
        "/api/abstractimage/$id/tile.$format"(controller: "restAbstractImage"){
            action = [GET:"tile"]
        }
        "/api/abstractimage/$id/crop.$format"(controller: "restAbstractImage"){
            action = [GET:"crop"]
        }
        "/api/imageinstance/$id/camera.$format"(controller: "restImageInstance"){
            action = [GET:"camera"]
        }
        "/api/abstractimage/$id/camera.$format"(controller: "restAbstractImage"){
            action = [GET:"camera"]
        }
        "/api/imageinstance/$id/camera_url-$x-$y-$w-$h.$format"(controller: "restImageInstance"){
            action = [GET:"cameraUrl"]
        }
        "/api/imageinstance/$id/window-$x-$y-$w-$h.$format"(controller: "restImageInstance"){
            action = [GET:"window"]
        }
        "/api/imageinstance/$id/window_url-$x-$y-$w-$h.$format"(controller: "restImageInstance"){
            action = [GET:"windowUrl"]
        }
        "/api/abstractimage/$id/associated.$format"(controller: "restAbstractImage"){
            action = [GET:"associated"]
        }
        "/api/abstractimage/$id/associated/$label.$format"(controller: "restAbstractImage"){
            action = [GET:"label"]
        }
//        "/api/abstractimage/$id/property.$format"(controller: "restAbstractImage"){
//            action = [GET:"imageProperties"]
//        }
//        "/api/abstractimage/$id/property/$imageproperty.$format"(controller: "restAbstractImage"){
//            action = [GET:"imageProperty"]
//        }
        "/api/abstractimage/$id/imageservers.$format"(controller: "restAbstractImage"){
            action = [GET:"imageServers"]
        }

        "/api/project/$id/image.$format"(controller: "restAbstractImage"){
            action = [GET:"listByProject"]
        }

        "/api/abstractimage/unused.$format"(controller:"restAbstractImage"){
            action = [GET: "listUnused"]
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


