package cytomine.web

class SecurityFilters {
    def springSecurityService

    def dependsOn = [APIAuthentificationFilters]

    def filters = {

        api(uri:'/api/**') {
            before = {
                if(!springSecurityService.isLoggedIn()) {
                    redirect(uri:'/')
                    return false
                }
            }
            after = {

            }
            afterView = {

            }
        }
    }

}


