class UrlMappings {

  static mappings = {
    "/$controller/$action?/$id?"{
      constraints {
        // apply constraints here
      }
    }

    /* HOME */
    "/"(view:"/index")

    /* ERRORS */
    "500"(view:'/error')

    /* USER */
    "/api/user"(controller:"restUser"){
      action = [GET:"list", POST:"save"]
    }
    "/api/user/$id"(controller:"restUser"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }
    "/api/user/current"(controller:"restUser"){
      action = [GET:"showCurrent"]
    }

    /* PROJECT */
    "/api/project"(controller: "restProject"){
      action = [GET:"list", POST:"save"]
    }
    "/api/project/$id"(controller: "restProject"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }
    "/api/project/$id/image"(controller: "restImage"){
      action = [GET:"showByProject"]
    }

    /* IMAGE */
    "/api/image"(controller: "restImage"){   //TO DO : fusionner scan & image
      action = [GET:"list", POST:"add"]
    }
    "/api/image/$id"(controller: "restImage"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }
    "/api/image/user/$id"(controller:"restImage"){
      action = [GET:"listByUser"]
    }
    "/api/image/$id/thumb"(controller: "restImage"){
      action = [GET:"thumb"]
    }
    "/api/image/$id/metadata"(controller: "restImage"){
      action = [GET:"metadata"]
    }


    /* Annotation */
    "/api/annotation"(controller:"restAnnotation"){
      action = [GET: "list",POST:"add"]
    }
    "/api/annotation/$id"(controller:"restAnnotation"){
      action = [GET:"show",PUT:"update", DELETE:"delete"]
    }
    "/api/annotation/$id/$zoom/crop"(controller: "restImage"){
      action = [GET:"crop"]
    }
    "/api/annotation/user/$id"(controller:"restAnnotation"){
      action = [GET:"listByUser"]
    }
    "/api/annotation/image/$id"(controller:"restAnnotation"){
      action = [GET:"listByImage"]
    }
    "/api/annotation/image/$idImage/user/$idUser"(controller:"restAnnotation"){
      action = [GET:"listByImageAndUser"]
    }



    "/api/annotation/$id/retrieval/$zoom/$maxsimilarpictures"(controller: "restImage") {
      action = [GET:"retrieval"]
    }


    "/api/term/$idterm/annotation"(controller:"restAnnotationTerm"){
      action = [GET: "listAnnotationByTerm"]
    }

    "/api/annotation/$idannotation/term"(controller:"restAnnotationTerm"){
      action = [GET: "listTermByAnnotation",POST:"add"]
    }
    "/api/annotation/$idannotation/term/$idterm"(controller:"restAnnotationTerm"){
      action = [GET:"show",DELETE:"delete"]
    }

    "/api/term"(controller:"restTerm"){
      action = [GET: "list",POST:"add"]
    }
     "/api/term/$id"(controller:"restTerm"){
      action = [GET:"show",PUT:"update", DELETE:"delete"]
    }

     "/api/ontology/$idontology/term"(controller:"restTermOntology"){
      action = [GET:"listTermByOntology"]
    }
     "/api/term/$idterm/ontology/$idontology"(controller:"restTermOntology"){
      action = [GET:"show", PUT:"update",DELETE:"delete"]
    }
     "/api/term/$idterm/ontology"(controller:"restTermOntology"){
      action = [GET:"listOntologyByTerm",POST:"add"]
    }
    

     /* Ontology */
    "/api/ontology"(controller:"restOntology"){
      action = [GET: "list",POST:"add"]
    }
    "/api/ontology/$id"(controller:"restOntology"){
      action = [GET:"show",PUT:"update", DELETE:"delete"]
    }

    "/api/relation"(controller: "restRelation"){
      action = [GET:"list", POST:"add"]
    }
    "/api/relation/$id"(controller: "restRelation"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }

    "/api/relationterm"(controller:"restRelationTerm"){
      action = [GET: "list",POST:"add"]
    }
    "/api/relationterm/$id"(controller:"restRelationTerm"){
      action = [GET:"show",PUT:"update", DELETE:"delete"]
    }

  }
}
