/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class DisciplineUrlMappings {

    static mappings = {
        "/api/discipline.$format"(controller:"restDiscipline"){
            action = [GET: "list",POST:"add"]
        }
        "/api/discipline/$id.$format"(controller:"restDiscipline"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
    }
}
