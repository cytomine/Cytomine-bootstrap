package cytomine.web

class SecurityFilters {
  def springSecurityService

  def filters = {
    all(controller:'*', action:'*') {
      /*before = {
         if(controllerName != "login" && actionName != "auth" && !springSecurityService.isLoggedIn()) {
            redirect(controller:'login', action:'auth')
            return false
         }
      }*/
      /*before = {
        def error403 = request.requestURI.endsWith("403")
        if(!error403 && (controllerName != "server" && actionName != "ping") && request.getParameter('action') != 'auth' && !springSecurityService.isLoggedIn()) {
          redirect(url:grailsApplication.config.grails.serverURL+"/?action=auth#login")
          return false
        }
      }*/
      after = {

      }
      afterView = {

      }
    }
  }

}


