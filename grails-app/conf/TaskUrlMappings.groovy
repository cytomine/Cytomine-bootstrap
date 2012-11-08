/**
 * Cytomine @ GIGA-ULG
 * User: lrollus
 * Date: 07/11/12
 * Time: 13:40
 */
class TaskUrlMappings {

    static mappings = {
        /* Task */
        "/api/task"(controller:"restTask"){
            action = [POST:"add"]
        }

        "/api/task/$id"(controller:"restTask"){
            action = [GET:"show"]
        }
    }
}
