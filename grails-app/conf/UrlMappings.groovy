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

        "/api/command" (controller : "command") {
            action = [GET:"list"]
        }

        "/processing/detect/$image/$x/$y"(controller:"processing") {
            action = [GET : "detect"]
        }
        "/processing/show/$image/$x/$y"(controller:"processing") {
            action = [GET : "show"]
        }
        "/api/import/imageproperties"(controller: "import") {
            action = [GET:"imageproperties"]
        }
        "/api/export/exportimages"(controller: "export") {
            action = [GET:"exportimages"]
        }

        "/api/storage" (controller:"restStorage") {
            //to do
        }
        "/api/storage/$id" (controller:"restStorage") {
            //to do
        }
    }
}
