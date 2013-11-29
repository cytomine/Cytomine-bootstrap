/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ProjectUrlMappings {


    static mappings = {
        "/api/project.$format"(controller: "restProject"){  //?.$format
            action = [GET:"list", POST:"add"]
        }
        "/api/project/$id.$format"(controller: "restProject"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/software/$id/project.$format"(controller:"restProject"){
            action = [GET: "listBySoftware"]
        }
        "/api/user/$id/project.$format"(controller:"restProject"){
            action = [GET:"listByUser"]
        }
        "/api/project/$id/last/$max.$format"(controller:"restProject"){
            action = [GET:"lastAction"]
        }
        "/api/ontology/$id/project.$format"(controller:"restProject"){
            action = [GET:"listByOntology"]
        }

        "/api/retrieval/$id/project.$format"(controller:"restProject"){
            action = [GET:"listRetrieval"]
        }

        "/api/user/$id/project/light.$format"(controller:"restProject"){
            action = [GET:"listLightByUser"]
        }

        "/api/project/method/lastopened.$format" (controller: "restProject") {
            action = [GET:"listLastOpened"]
        }
    }
}
