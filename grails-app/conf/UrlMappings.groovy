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
    "500"(view:'/error')

    /* User */
    "/api/user"(controller:"restUser"){
      action = [GET:"list", POST:"save"]
    }
    "/api/user/$id"(controller:"restUser"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }
    "/api/user/current"(controller:"restUser"){
      action = [GET:"showCurrent"]
    }
    "/api/user/$id/image"(controller:"restImage"){
      action = [GET:"listByUser"]
    }
    "/api/user/$id/annotation/"(controller:"restAnnotation"){
      action = [GET:"listByUser"]
    }
    "/api/user/$idUser/image/$idImage/annotation"(controller:"restAnnotation"){
      action = [GET:"listByImageAndUser"]
    }
    //TODO: /user/$id/project


    /* Project */
    "/api/project"(controller: "restProject"){
      action = [GET:"list", POST:"save"]
    }
    "/api/project/$id"(controller: "restProject"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }
    "/api/project/$id/image"(controller: "restImage"){
      action = [GET:"showByProject"]
    }
    //TODO: /project/$id/user

    /* Image */
    "/api/image"(controller: "restImage"){
      action = [GET:"list", POST:"add"]
    }
    "/api/image/$id"(controller: "restImage"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }
    "/api/image/$id/thumb"(controller: "restImage"){
      action = [GET:"thumb"]
    }
    "/api/image/$id/metadata"(controller: "restImage"){
      action = [GET:"metadata"]
    }
    "/api/image/$id/annotation"(controller:"restAnnotation"){
      action = [GET:"listByImage"]
    }
    "/api/image/$id/term"(controller:"restTerm"){
      action = [GET:"listTermByImage"]
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
    "/api/annotation/$id/retrieval/$zoom/$maxsimilarpictures"(controller: "restImage") {
      action = [GET:"retrieval"]
    }
    "/api/annotation/$idannotation/term"(controller:"restAnnotationTerm"){
      action = [GET: "listTermByAnnotation",POST:"add"]
    }
    "/api/annotation/$idannotation/term/$idterm"(controller:"restAnnotationTerm"){
      action = [GET:"show",DELETE:"delete"]
    }

    /* Term */
    "/api/term"(controller:"restTerm"){
      action = [GET: "list",POST:"add"]
    }
     "/api/term/$id"(controller:"restTerm"){
      action = [GET:"show",PUT:"update", DELETE:"delete"]
    }
    "/api/term/$idterm/annotation"(controller:"restAnnotationTerm"){
      action = [GET: "listAnnotationByTerm"]
    }
    "/api/term/$idterm/ontology"(controller:"restOntology"){
      action = [GET:"listOntologyByTerm"]
    }

    /* Ontology */
    "/api/ontology"(controller:"restOntology"){
      action = [GET: "list",POST:"add"]
    }
    "/api/ontology/$id"(controller:"restOntology"){
      action = [GET:"show",PUT:"update", DELETE:"delete"]
    }
     "/api/ontology/$idontology/term"(controller:"restTerm"){
      action = [GET:"listTermByOntology"]
    }


    /* Relation (term)*/
    //TODO: Implement (see AnnotationTerm for template)
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
