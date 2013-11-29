/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:52
 */
class AnnotationTermUrlMappings {

    static mappings = {
        "/api/annotation/$idannotation/term.$format"(controller:"restAnnotationTerm"){
            action = [GET: "listTermByAnnotation"]
        }
        "/api/annotation/$idannotation/term/$idterm.$format"(controller:"restAnnotationTerm"){
            action = [POST:"add",DELETE:"delete", GET:"show"]
        }
        "/api/annotation/$idannotation/term/$idterm/clearBefore.$format"(controller:"restAnnotationTerm"){
            action = [POST:"addWithDeletingOldTerm"]
        }
        "/api/annotation/$idannotation/term/$idterm/user/$idUser.$format"(controller:"restAnnotationTerm"){
            action = [GET:"show",DELETE:"delete"]
        }
        "/api/annotation/$idannotation/notuser/$idNotUser/term.$format"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationTermByUserNot"]
        }
    }
}


