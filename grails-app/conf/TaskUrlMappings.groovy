/**
 * Cytomine @ GIGA-ULG
 * User: lrollus
 * Date: 07/11/12
 * Time: 13:40
 */
class TaskUrlMappings {

    static mappings = {
        /* Task */
        "/api/task.$format"(controller:"restTask"){
            action = [POST:"add"]
        }

        "/api/task/$id.$format"(controller:"restTask"){
            action = [GET:"show"]
        }

        "/api/project/$idProject/task/comment.$format"(controller:"restTask"){
            action = [GET:"listCommentByProject"]
        }
    }
}
