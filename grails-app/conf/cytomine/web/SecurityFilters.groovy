package cytomine.web

class SecurityFilters {
    def springSecurityService

    def dependsOn = [APIAuthentificationFilters]

    def filters = {
        all(uri:'/api/**') {
            before = {
                println "SecurityFilters.before()"
                println "springSecurityService.isLoggedIn()="+springSecurityService.isLoggedIn()
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


