class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        /* HOME */
        "/"(view:"/index")

        /* ERROS */
        "500"(view:'/error')

        /* API MAPPINGS */
        "/api/users.$format"(controller:"restUser"){
            action = [GET:"list", POST:"save"]
        }
        "/api/user/$id.$format"(controller:"restUser"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/scan.$format"(controller: "restScan"){
            action = [GET:"list", POST:"save"]
        }
        "/api/scan/$id.$format"(controller: "restScan"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
       "/api/image/thumb/$idscan"(controller: "restImage"){
            action = [GET:"thumb"]
        }
        "/api/image/metadata/$idscan"(controller: "restImage"){
            action = [GET:"metadata"]
        }
        "/api/image/crop/$idscan/$topleftx/$toplefty/$width/$height/$zoom"(controller: "restImage"){
            action = [GET:"crop"]
        }
        "/api/projects.$format"(controller: "restProject"){
            action = [GET:"list"]
        }
        "/api/image/retrieval/$idscan/$maxsimilarpictures"(controller: "restImage") {
            action = [GET:"retrieval"]
        }
	}
}
