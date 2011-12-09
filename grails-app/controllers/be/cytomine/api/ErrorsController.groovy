package be.cytomine.api

import grails.plugins.springsecurity.Secured

    @Secured(['permitAll'])
    class ErrorsController extends RestController{

       def error403 = {
            response.status = 403
            render "FORBIDDEN"
       }

       def error404 = {}

       def error500 = {
          render view: '/error'
       }

}
