/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UploadedFileUrlMappings {

    static mappings = {

        "/api/uploadedfile"(controller:"restUploadedFile"){
            action = [GET: "list",POST:"add"]
        }
        "/api/uploadedfile/$id"(controller:"restUploadedFile"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }


//        "/uploadedfile" (controller:"restUploadedFile"){
//            action = [POST : "add"]
//        }
    }
}
