/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ProjectUrlMappings {

    static mappings = {
        "/api/project"(controller: "restProject"){
            action = [GET:"list", POST:"add"]
        }
        "/api/project/$id"(controller: "restProject"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/software/$id/project"(controller:"restProject"){
            action = [GET: "listBySoftware"]
        }
        "/api/user/$id/project"(controller:"restProject"){
            action = [GET:"listByUser"]
        }
        "/api/project/$id/last/$max"(controller:"restProject"){
            action = [GET:"lastAction"]
        }
        "/api/ontology/$id/project"(controller:"restProject"){
            action = [GET:"listByOntology"]
        }
        "/api/discipline/$id/project"(controller:"restProject"){
            action = [GET:"listByDiscipline"]
        }
    }
}
