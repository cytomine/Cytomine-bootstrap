package cytomine.web

class SecurityFilters {
    def springSecurityService

    def filters = {
        all(controller:'*', action:'*') {
            before = {
               /*if(controllerName != "login" && actionName != "auth" && !springSecurityService.isLoggedIn()) {
                  redirect(controller:'login', action:'auth')
                  return false
               } */
            }
            after = {
                
            }
            afterView = {
                
            }
        }
    }
    
}


