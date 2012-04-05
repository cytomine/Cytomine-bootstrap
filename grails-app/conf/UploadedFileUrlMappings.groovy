/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UploadedFileUrlMappings {

    static mappings = {
        "/api/uploadedfile" (controller:"restUploadedFile"){
            action = [POST : "add", GET : "list"]
        }
        "/api/uploadedfile/$id" (controller:"restUploadedFile"){
            action = [GET : "show"]
        }
    }
}
