/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UploadedFileUrlMappings {

    static mappings = {

        "/api/uploadedfile.$format"(controller:"restUploadedFile"){
            action = [GET: "list",POST:"add"]
        }
        "/api/uploadedfile/$id.$format"(controller:"restUploadedFile"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }


//        "/uploadedfile" (controller:"restUploadedFile"){
//            action = [POST : "add"]
//        }
    }
}
