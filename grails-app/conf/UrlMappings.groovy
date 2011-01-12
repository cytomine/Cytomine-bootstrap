class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
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
		"/"(view:"/index")
		"500"(view:'/error')
	}
}
