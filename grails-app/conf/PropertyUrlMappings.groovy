
class PropertyUrlMappings
{
    static mappings = {

        "/api/user/$idUser/imageinstance/$idImage/annotationposition"(controller:"restProperty"){
            action = [GET:"listAnnotationPosition"]
        }

        //project
        "/api/project/$idProject/property"(controller:"restProperty"){
            action = [GET:"listByProject",POST: "addPropertyProject"]
        }
        "/api/project/$idProject/key/$key/property"(controller:"restProperty"){
            action = [GET:"showProject"]
        }
        "/api/project/$idProject/property/$id"(controller:"restProperty"){
            action = [GET:"showProject",PUT:"update", DELETE:"delete"]
        }

        //annotation
        "/api/annotation/$idAnnotation/property"(controller:"restProperty"){
            action = [GET:"listByAnnotation",POST: "addPropertyAnnotation"]
        }
        "/api/annotation/$idAnnotation/key/$key/property"(controller:"restProperty"){
            action = [GET:"showAnnotation"]
        }
        "/api/annotation/$idAnnotation/property/$id"(controller:"restProperty"){
            action = [GET:"showAnnotation",PUT:"update", DELETE:"delete"]
        }
        "/api/annotation/property/key"(controller:"restProperty"){
            action = [GET:"listKeyForAnnotation"]
        }

        //IMAGEINSTANCE
        "/api/imageinstance/$idImageInstance/property"(controller:"restProperty"){
            action = [GET:"listByImageInstance",POST: "addPropertyImageInstance"]
        }
        "/api/imageinstance/$idImageInstance/key/$key/property"(controller:"restProperty"){
            action = [GET:"showImageInstance"]
        }
        "/api/imageinstance/$idImageInstance/property/$id"(controller:"restProperty"){
            action = [GET:"showImageInstance",PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/property/key"(controller:"restProperty"){
            action = [GET:"listKeyForImageInstance"]
        }
    }
}
