package be.cytomine.api

import grails.plugins.springsecurity.Secured

    @Secured(['permitAll'])
    class ErrorsController extends RestController{

       def error403 = {
            response.status = 403
            render(contentType: 'text/json') {
                errors(message: "You are not allowed to do this!")
            }

       }

       def error404 = {}

       def error500 = {
          render view: '/error'
       }

}
