/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:52
 */
class AnnotationTermUrlMappings {

    static mappings = {

        "/api/annotation/$idannotation/term/$idterm"(controller:"restAnnotationTerm"){
            action = [POST:"add",DELETE:"delete", GET:"show"]
        }
        "/api/annotation/$idannotation/term/$idterm/user/$idUser"(controller:"restAnnotationTerm"){
            action = [GET:"show"]
        }
    }
}


