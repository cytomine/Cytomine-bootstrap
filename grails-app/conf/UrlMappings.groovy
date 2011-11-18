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
        "/500" (view:'/error')
        "/403" (view:'/forbidden')

        /* FILTERS */
        "/api/user/$id/image"(controller:"restImage"){
            action = [GET:"listByUser"]
        }
        "/api/user/$id/imageinstance"(controller:"restImageInstance"){
            action = [GET:"listByUser"]
        }
        "/api/user/$id/annotation"(controller:"restAnnotation"){
            action = [GET:"listByUser"]
        }
        "/api/user/$idUser/imageinstance/$idImage/annotation"(controller:"restAnnotation"){
            action = [GET:"listByImageAndUser"]
        }
        "/api/user/$id/project"(controller:"restProject"){
            action = [GET:"listByUser"]
        }
        "/api/currentuser/project"(controller:"restProject"){
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
        "/api/project/$project/imagefilter"(controller: "restImageFilterProject"){
            action = [GET:"list", POST : "add"]
        }
        "/api/project/$project/imagefilter/$imageFilter"(controller: "restImageFilterProject"){
            action = [DELETE : "delete"]
        }
        "/api/project/$id/imageinstance"(controller: "restImageInstance"){
            action = [GET:"listByProject"]
        }
        "/api/project/$id/annotation"(controller: "restAnnotation"){
            action = [GET:"listByProject"]
        }
        "/api/project/$idproject/slide"(controller:"restProjectSlide"){
            action = [GET: "listSlideByProject"]
        }
        "/api/project/$idProject/term"(controller:"restTerm"){
            action = [GET:"listAllByProject"]
        }
        "/api/imageinstance/$id/annotation"(controller:"restAnnotation"){
            action = [GET:"listByImage"]
        }
        "/api/imageinstance/$id/term"(controller:"restTerm"){
            action = [GET:"listByImageInstance"]
        }
        "/api/image/$id/imageinstance"(controller: "restImageInstance"){
            action = [GET:"listByImage"]
        }
        "/api/image/$idabstractimage/group"(controller:"restGroup"){
            action = [GET: "listGroupByAbstractImage"]
        }

        "/api/project/$idproject/image/$idimage/imageinstance"(controller:"restImageInstance"){
            action = [GET:"showByProjectAndImage",DELETE:"delete"]
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
        "/api/project/$id/last/$max"(controller:"restProject"){
            action = [GET:"lastAction"]
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
            action = [GET: "listAnnotationTermByUser"]
        }
        "/api/annotation/$idannotation/term/$idterm/clearBefore"(controller:"restAnnotationTerm"){
            action = [POST:"addWithDeletingOldTerm"]
        }

        "/api/annotation/$idannotation/retrieval"(controller:"restRetrieval"){
            action = [GET:"listSimilarAnnotationAndBestTerm",POST:"index"]
        }

        "/api/annotation/term/suggest"(controller:"restSuggestedTerm"){
            action = [GET:"list"]
        }
        "/api/annotation/$idannotation/term/suggest"(controller:"restSuggestedTerm"){
            action = [GET:"list",POST:"add"]
        }
        "/api/annotation/$idannotation/term/$idterm/job/$idjob/suggest"(controller:"restSuggestedTerm"){
            action = [GET:"show", DELETE:"delete"]
        }

        "/api/project/$idproject/annotation/term/suggest"(controller:"restSuggestedTerm"){
            action = [GET:"worstAnnotation"]
        }
        "/api/project/$idproject/term/suggest"(controller:"restSuggestedTerm"){
            action = [GET:"worstTerm"]
        }

        "/api/term/$idterm/annotation"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationByTerm"]
        }
        "/api/term/$idterm/annotation"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationByTerm"]
        }
        "/api/term/$idterm/project/$idproject/annotation"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationByProjectAndTerm"]
        }
        "/api/term/$idterm/imageinstance/$idimageinstance/annotation"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationByProjectAndImageInstance"]
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

        "/api/software"(controller:"restSoftware") {
            action = [GET:"list"]
        }

        "/api/software/$id"(controller:"restSoftware"){
            action = [GET:"show"]
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
        "/api/project/$id/annotation/download"(controller: "restAnnotation"){
            action = [GET:"downloadDocumentByProject"]
        }



    }
}
