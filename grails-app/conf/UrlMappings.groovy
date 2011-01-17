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
        "/api/user.$format"(controller:"restuser"){
            action = [GET:"list", POST:"save"]
        }
        "/api/user/$id.$format"(controller:"restuser"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/scan.$format"(controller:"restscan"){
            action = [GET:"list", POST:"save"]
        }
        "/api/scan/$id.$format"(controller:"restscan"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
       "/api/image/thumb/$idscan"(controller:"restimage"){
            action = [GET:"thumb"]
        }
        "/api/image/metadata/$idscan"(controller:"restimage"){
            action = [GET:"metadata"]
        }
        "/api/image/crop/$idscan/$topleftx/$toplefty/$width/$height/$zoom"(controller:"restimage"){
            action = [GET:"crop"]
        }
	}
}
