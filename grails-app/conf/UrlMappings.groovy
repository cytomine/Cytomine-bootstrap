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
      action = [GET:"list", POST:"add"]
    }
    "/api/project/$id"(controller: "restProject"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }
    "/api/project/$id/image"(controller: "restImage"){
      action = [GET:"listByProject"]
    }
    "/api/project/$id/user"(controller: "restUser"){
      action = [GET:"showByProject"]
    }
    "/api/project/$id/annotation"(controller: "restAnnotation"){
      action = [GET:"listByProject"]
    }
    //TODO:  + add current user


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
      action = [GET:"listByImage"]
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
      action = [GET: "listTermByAnnotation"]
    }
    "/api/annotation/$idannotation/term/$idterm"(controller:"restAnnotationTerm"){
      action = [GET:"show",DELETE:"delete",POST:"add"]
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
    "/api/term/$id/ontology"(controller:"restOntology"){
      action = [GET:"listByTerm"]
    }

    /* Ontology */
    "/api/ontology"(controller:"restOntology"){
      action = [GET: "list",POST:"add"]
    }
    "/api/ontology/$id"(controller:"restOntology"){
      action = [GET:"show",PUT:"update", DELETE:"delete"]
    }
    "/api/ontology/$idontology/term"(controller:"restTerm"){
      action = [GET:"listByOntology"]
    }
    "/api/ontology/$id/tree"(controller:"restOntology"){
      action = [GET:"tree"]
    }


    /* Relation (term)*/
    //TODO: Implement (see AnnotationTerm for template)
    "/api/relation"(controller: "restRelation"){
      action = [GET:"list", POST:"add"]
    }
    "/api/relation/$id"(controller: "restRelation"){
      action = [GET:"show", PUT:"update", DELETE:"delete"]
    }


    "/api/relation/$id/term"(controller:"restRelationTerm"){
      action = [GET: "listByRelation",POST:"add"]
    }
    "/api/relation/$idrelation/term1/$idterm1/term2/$idterm2"(controller:"restRelationTerm"){
      action = [GET: "show",DELETE:"delete"]
    }
    //i = 1 or 2 (term 1 or term 2), id = id term
    "/api/relation/term/$i/$id"(controller:"restRelationTerm"){
      action = [GET: "listByTerm"]
    }

  }
}
