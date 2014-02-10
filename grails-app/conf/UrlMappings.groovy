import be.cytomine.Exception.ForbiddenException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.model.NotFoundException

class UrlMappings {

    static mappings = {     //?.$format
        "/$controller/$action?/$id?.$format?"{
            constraints {
                // apply constraints here
            }
        }
        "/$controller/$action?.$format?"{
            constraints {
                // apply constraints here
            }
        }
        "/jsondoc" (controller : "jsondoc")
        "/admin/manage/$action?.$format"(controller: "adminManage")
        "/adminManage/$action?.$format"(controller: "errors", action: "error500")

        /* Home */
        "/"(view:"/index")

        /* Errors */
//        "/500" (view:'/error')
        //        "/403" (view:'/forbidden')
        "403"(controller: "errors", action: "error403")
        //"404.$format"(controller: "errors", action: "error404")
        "500"(controller: "errors", action: "error500")
        "500"(controller: "errors", action: "error403", exception: AccessDeniedException)
        "500"(controller: "errors", action: "error403", exception: NotFoundException)
        "500"(controller: "errors", action: "error403", exception: ForbiddenException)
 //       "500.$format"(controller: "errors", action: "error404", exception: ObjectNotFoundException)

        "/processing/detect/$image/$x/$y.$format"(controller:"processing") {
            action = [GET : "detect"]
        }
        "/processing/show/$image/$x/$y.$format"(controller:"processing") {
            action = [GET : "show"]
        }
        "/api/import/imageproperties.$format"(controller: "import") {
            action = [GET:"imageproperties"]
        }
        "/api/export/exportimages.$format"(controller: "export") {
            action = [GET:"exportimages"]
        }

        "/api/project/$id/commandhistory.$format"(controller: "restProject") {
            action = [GET:"listCommandHistory"]
        }
        "/api/commandhistory.$format"(controller: "restProject") {
            action = [GET:"listCommandHistory"]
        }

        "/api/search.$format"(controller: "search") {
            action = [GET:"listResponse"]
        }

        "/api/news.$format"(controller:"news") {
            action = [GET:"listNews"]
        }

        "/loginWithoutLDAP/login" (controller: "login") {
            action = [GET:"loginWithoutLDAP"]
        }
    }
}
