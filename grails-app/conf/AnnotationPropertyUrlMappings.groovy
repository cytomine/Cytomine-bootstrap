
class AnnotationPropertyUrlMappings
{
    static mappings = {
        "/api/annotationproperty"(controller:"restAnnotationProperty"){
            action = [GET:"list",POST: "add"]
        }
        "/api/annotationproperty/$id"(controller:"restAnnotationProperty"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/annotationproperty/key"(controller:"restAnnotationProperty"){
            action = [GET:"listKey"]
        }
        "/api/annotation/$idAnnotation/annotationproperty"(controller: "restAnnotationProperty"){
            action = [GET:"listByAnnotation"]
        }
        "/api/annotation/$idAnnotation/annotationproperty/$key"(controller: "restAnnotationProperty"){
            action = [GET:"show"]
        }
    }
}
