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

        /* API MAPPINGS */
        "/api/user"(controller:"restUser"){
            action = [GET:"list", POST:"save"]
        }
        "/api/user/$id"(controller:"restUser"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/scan"(controller: "restScan"){
            action = [GET:"list", POST:"save"]
        }
        "/api/scan/$id"(controller: "restScan"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
       "/api/image/thumb/$idscan"(controller: "restImage"){
            action = [GET:"thumb"]
        }
        "/api/image/metadata/$idscan"(controller: "restImage"){
            action = [GET:"metadata"]
        }
        "/api/image/crop/$zoom/$idannotation"(controller: "restImage"){
            action = [GET:"crop"]
        }
        "/api/image/crop/$idannotation"(controller: "restImage"){
            action = [GET:"crop"]
        }
        "/api/projects"(controller: "restProject"){
            action = [GET:"list"]
        }
        "/api/image/retrieval/$maxsimilarpictures/$idannotation/"(controller: "restImage") {
            action = [GET:"retrieval"]
        }
        "/api/image/retrieval/$zoom/$maxsimilarpictures/$idannotation/"(controller: "restImage") {
            action = [GET:"retrieval"]
        }
        /* Annotation */
        "/api/annotation"(controller:"restAnnotation"){
            action = [GET:"list"]
        }
        /* Annotation */
        "/api/annotation/scan/$idscan"(controller:"restAnnotation"){
            action = [GET:"list"]
        }
        "/api/annotation/$idannotation"(controller:"restAnnotation"){
            action = [GET:"show"]
        }

        //must be merge with the previous block
        "/api/annotation/scan/$idscan/$location"(controller:"restAnnotation"){
            action = [POST:"add"]
        }
       //must be merge with the previous block
        "/api/annotation/$idannotation/$location"(controller:"restAnnotation"){
            action = [PUT:"update"]
        }
       //must be merge with the previous block
        "/api/annotation/$idannotation"(controller:"restAnnotation"){
            action = [DELETE:"delete"]
        }
	}
}
