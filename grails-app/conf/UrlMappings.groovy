/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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

        "/api/search-engine.$format"(controller: "searchEngine") {
            action = [GET:"search"]
        }
        "/api/search-result.$format"(controller: "searchEngine") {
            action = [GET:"result"]
        }

        "/api/image/$className/$id.$format"(controller:"searchEngine") {
            action = [GET:"redirectToImageURL"]
        }
        "/api/url/$className/$id.$format"(controller:"searchEngine") {
            action = [GET:"redirectToGoToURL"]
        }



        "/api/news.$format"(controller:"news") {
            action = [GET:"listNews"]
        }

        "/loginWithoutLDAP/login" (controller: "login") {
            action = [GET:"loginWithoutLDAP"]
        }

        "/login/forgotPassword" (controller: "login") {
            action = [POST:"forgotPassword"]
        }

        "/login/loginWithToken" (controller: "login") {
            action = [GET:"loginWithToken"]
        }


        "/session/admin/open.$format" (controller: "grantRole") {
            action = [GET:"openAdminSession"]
        }
        "/session/admin/close.$format" (controller: "grantRole") {
            action = [GET:"closeAdminSession"]
        }
        "/session/admin/info.$format" (controller: "grantRole") {
            action = [GET:"infoAdminSession"]
        }

        "/custom-ui/config.$format" (controller: "customUI") {
            action = [GET:"retrieveUIConfig"]
        }

//        "/custom-ui/roles.$format" (controller: "customUI") {
//            action = [GET:"retrieveUIRoles"]
//        }

        "/custom-ui/project/$project.$format" (controller: "customUI") {
            action = [GET:"showCustomUIForProject",POST:"addCustomUIForProject"]
        }

//        "/custom-ui/project/$project/flag.$format" (controller: "customUI") {
//            action = [GET:"retrieveProjectUIConfig"]
//        }

        "/status.json" (controller: "server") {
            action = [GET:"status"]
        }
        "/ldap.$format" (controller: "restConfig") {
            action = [GET:"ldap"]
        }
    }
}
