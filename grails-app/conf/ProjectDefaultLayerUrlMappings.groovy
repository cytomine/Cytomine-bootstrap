/**
 * Created by hoyoux on 13.11.14.
 */
class ProjectDefaultLayerUrlMappings {

    static mappings = {
        "/api/project/$idProject/defaultlayer.$format"(controller:"restProjectDefaultLayer"){
            action = [GET: "listByProject",POST:"add"]
        }
        "/api/project/$idProject/defaultlayer/$id.$format"(controller:"restProjectDefaultLayer"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
    }
}
