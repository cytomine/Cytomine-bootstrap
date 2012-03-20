import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.access.AccessDeniedException

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }


        /* Home */
        "/"(view:"/index")

        /* Errors */
//        "/500" (view:'/error')
        //        "/403" (view:'/forbidden')
        "403"(controller: "errors", action: "error403")
        //"404"(controller: "errors", action: "error404")
        "500"(controller: "errors", action: "error500")
        "500"(controller: "errors", action: "error403", exception: AccessDeniedException)
        "500"(controller: "errors", action: "error403", exception: NotFoundException)


        /* FILTERS */
        "/api/user/$id/image"(controller:"restImage"){
            action = [GET:"listByUser"]
        }

        "/api/user/$id/project"(controller:"restProject"){
            action = [GET:"listByUser"]
        }

        "/api/currentuser/ontology"(controller:"restOntology"){
            action = [GET:"listByUser"]
        }
        "/api/currentuser/ontology/light"(controller:"restOntology"){  //TODO: merge with previous block
            action = [GET: "listByUserLight"]
        }
        "/api/currentuser/image"(controller: "restImage"){
            action = [GET:"listByUser"]
        }
        "/api/currentuser/slide"(controller: "restSlide"){
            action = [GET:"listByUser"]
        }
        "/api/project/$id/image"(controller: "restImage"){
            action = [GET:"listByProject"]
        }
        "/api/project/imagefilter"(controller: "restImageFilter"){
            action = [GET:"list"]
        }
        "/api/project/imagefilter/$id"(controller: "restImageFilter"){
            action = [GET:"show"]
        }

        "/api/project/$project/imagefilterproject"(controller: "restImageFilterProject"){
            action = [GET:"listByProject"]
        }
        "/api/imagefilterproject" (controller: "restImageFilterProject"){
            action = [GET:"list", POST : "add"]
        }
        "/api/imagefilterproject/$id"(controller: "restImageFilterProject"){
            action = [DELETE : "delete"]
        }



        "/api/project/$idproject/slide"(controller:"restProjectSlide"){
            action = [GET: "listSlideByProject"]
        }
        "/api/project/$idProject/term"(controller:"restTerm"){
            action = [GET:"listAllByProject"]
        }

        "/api/imageinstance/$id/position" (controller : "restUserPosition") {
            action = [POST:"add"]
        }
        "/api/imageinstance/$id/position/$user" (controller : "restUserPosition") {
            action = [GET:"lastPositionByUser"]
        }
        "/api/imageinstance/$id/online"(controller: "restUserPosition"){
            action = [GET:"listOnlineUsersByImage"]
        }
        "/api/project/$id/online"(controller:"restUserPosition") {
            action = [GET : "listLastUserPositionsByProject"]
        }
        "/api/imageinstance/$id/term"(controller:"restTerm"){
            action = [GET:"listByImageInstance"]
        }
        "/api/image/$idabstractimage/group"(controller:"restGroup"){
            action = [GET: "listGroupByAbstractImage"]
        }

        //TODO:  + add current user
        "/api/project/$id/stats/term"(controller:"stats"){
            action = [GET:"statTerm"]
        }
        "/api/project/$id/stats/user"(controller:"stats"){
            action = [GET:"statUser"]
        }
        "/api/project/$id/stats/termslide"(controller:"stats"){
            action = [GET:"statTermSlide"]
        }
        "/api/project/$id/stats/userslide"(controller:"stats"){
            action = [GET:"statUserSlide"]
        }
        "/api/project/$id/stats/userannotations"(controller:"stats"){
            action = [GET:"statUserAnnotations"]
        }
        "/api/project/$id/stats/retrievalsuggestion"(controller:"stats"){
            action = [GET:"statRetrievalsuggestion"]
        }
        "/api/project/$id/last/$max"(controller:"restProject"){
            action = [GET:"lastAction"]
        }

        "/api/stats/retrieval/avg"(controller:"stats"){
            action = [GET:"statRetrievalAVG"]
        }
        "/api/stats/retrieval/confusionmatrix"(controller:"stats"){
            action = [GET:"statRetrievalConfusionMatrix"]
        }
        "/api/stats/retrieval/worstTerm"(controller:"stats"){
            action = [GET:"statRetrievalWorstTerm"]
        }
        "/api/stats/retrieval/worstTermWithSuggest"(controller:"stats"){
            action = [GET:"statWorstTermWithSuggestedTerm"]
        }


        "/api/stats/retrieval/worstAnnotation"(controller:"stats"){
            action = [GET:"statRetrievalWorstAnnotation"]
        }

        "/api/stats/retrieval/evolution"(controller:"stats"){
            action = [GET:"statRetrievalEvolution"]
        }

        "/api/image/$idabstractimage/group/$idgroup"(controller:"restAbstractImageGroup"){
            action = [GET:"show",DELETE:"delete",POST:"add"]
        }

        "/api/annotation/$id/$zoom/crop"(controller: "restImage"){
            action = [GET:"crop"]
        }
        "/api/annotation/$id/crop"(controller: "restImage"){
            action = [GET:"crop"]
        }
        "/api/annotation/$id/retrieval/$zoom/$maxsimilarpictures"(controller: "restImage") {
            action = [GET:"retrieval"]
        }
        "/api/annotation/$idannotation/term"(controller:"restAnnotationTerm"){
            action = [GET: "listTermByAnnotation"]
        }
        "/api/annotation/$idannotation/user/$idUser/term"(controller:"restAnnotationTerm"){
            action = [GET: "listTermByAnnotation"]
        }
        "/api/annotation/$idannotation/notuser/$idNotUser/term"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationTermByUserNot"]
        }
        "/api/annotation/$idannotation/term/$idterm/clearBefore"(controller:"restAnnotationTerm"){
            action = [POST:"addWithDeletingOldTerm"]
        }

        "/api/annotation/$idannotation/retrieval"(controller:"restRetrieval"){
            action = [GET:"listSimilarAnnotationAndBestTerm",POST:"index"]
        }

        "/api/term/$id/ontology"(controller:"restOntology"){
            action = [GET:"listByTerm"]
        }
        "/api/term/$id/project/stat"(controller:"restTerm"){
            action = [GET:"statProject"]
        }


        "/api/ontology/$id/project"(controller:"restProject"){
            action = [GET:"listByOntology"]
        }
        "/api/ontology/$idontology/term"(controller:"restTerm"){
            action = [GET:"listByOntology"]
        }
        "/api/ontology/$idontology/term"(controller:"restTerm"){
            action = [GET:"listAllByOntology"]
        }




        "/api/relation/$id/term"(controller:"restRelationTerm"){
            action = [GET: "listByRelation",POST:"add"]
        }
        "/api/relation/$idrelation/term1/$idterm1/term2/$idterm2"(controller:"restRelationTerm"){
            action = [GET: "show",DELETE:"delete"]
        }
        "/api/relation/parent/term"(controller:"restRelationTerm"){
            action = [GET: "listByRelation",POST:"add"]
        }
        "/api/relation/parent/term1/$idterm1/term2/$idterm2"(controller:"restRelationTerm"){
            action = [GET: "show",DELETE:"delete"]
        }
        "/api/relation/term/$id"(controller:"restRelationTerm"){
            action = [GET: "listByTermAll"]
        }
        //i = 1 or 2 (term 1 or term 2), id = id term
        "/api/relation/term/$i/$id"(controller:"restRelationTerm"){
            action = [GET: "listByTerm"]
        }

        /* Slide */
        "/api/slide"(controller: "restSlide"){
            action = [GET:"list", POST:"add"]
        }
        "/api/slide/$id"(controller: "restSlide"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }

        "/api/slide/$idslide/project"(controller:"restProjectSlide"){
            action = [GET: "listProjectBySlide"]
        }

        "/api/storage" (controller:"restStorage") {
            //to do
        }

        "/api/storage/$id" (controller:"restStorage") {
            //to do
        }

        "/api/import/annotations/$idProject"(controller: "import") {
            action = [GET:"annotations"]
        }

        "/api/import/imageproperties"(controller: "import") {
            action = [GET:"imageproperties"]
        }

        "/api/export/exportimages"(controller: "export") {
            action = [GET:"exportimages"]
        }

        "/api/job"(controller:"restJob") {
            action = [GET:"list", POST:"save"]
        }

        "/api/job/$id"(controller:"restJob"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }


        "/processing/detect/$image/$x/$y"(controller:"processing") {
            action = [GET : "detect"]
        }

        "/processing/show/$image/$x/$y"(controller:"processing") {
            action = [GET : "show"]
        }

        "/api/project/$id/user"(controller: "restUser"){
            action = [GET:"showByProject",POST:"addUser"]
        }
        "/api/project/$id/user/$idUser"(controller: "restUser"){
            action = [DELETE:"deleteUser",POST:"addUser"]
        }
        "/api/project/$id/user/$idUser/admin"(controller: "restUser"){
            action = [DELETE:"deleteUserAdmin",POST:"addUserAdmin"]
        }


        "/api/command" (controller : "command") {
            action = [GET:"list"]
        }

        "/api/downloadPDF" (controller : "stats") {
            action = [GET:"convertHtmlContentToPDF"]
        }

    }
}
