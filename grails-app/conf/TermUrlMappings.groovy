/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class TermUrlMappings {

    static mappings = {
        /* Term */
        "/api/term"(controller:"restTerm"){
            action = [GET: "list",POST:"add"]
        }
        "/api/term/$id"(controller:"restTerm"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
    }
}

