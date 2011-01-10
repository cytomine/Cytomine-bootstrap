package be.cytomine.api

import be.cytomine.security.User
import grails.converters.*
import be.cytomine.security.SecUserSecRole

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 6/01/11
 * Time: 20:27
 */
class RestuserController {

  def springSecurityService

  /* REST API */

    def list = {
       def data = [:]
       data.user = User.list()
       if (params.format.toLowerCase() == "json") {
          render data as JSON
       } else if (params.format.toLowerCase() == "xml") {
          render data as XML
       }
    }

    def show = {
      if(params.id && User.exists(params.id)) {
        def data = User.findById(params.id)
        if (params.format.toLowerCase() == "json") {
          render data as JSON
        } else if (params.format.toLowerCase() == "xml") {
          render data as XML
        }
      } else {
        SendNotFoundResponse()
      }

    }

    def save = {
      def user = new User()
      def data
      if (params.format.toLowerCase() == "json") {
        data = request.JSON
        user.username = data.user.username
        user.firstname = data.user.firstname
        user.lastname = data.user.lastname
        user.email = data.user.email
        user.password = springSecurityService.encodePassword(data.user.password)
        user.enabled = true
      }
      else if (params.format.toLowerCase() == "xml") {
        data = request.XML
        user.username = data.user.username.user.text()
        user.firstname = data.user.firstname.text()
        user.lastname = data.user.lastname.text()
        user.email = data.user.email.text()
        user.password = springSecurityService.encodePassword(data.user.password.text())
        user.enabled = true
      }

      if (user.validate()) {
        user.save();
        response.status = 201
        if (params.format.toLowerCase() == "json")
          render user as JSON
        else if (params.format.toLowerCase() == "xml")
          render user as XML
      } else {
        sendValidationFailedResponse(user, 403)
      }
    }

    def update = {
      User user = User.get(params.id)
      def data
      if (!user) {
        SendNotFoundResponse()
      }

      if (params.format.toLowerCase() == "json") {
        data = request.JSON
        user.username = data.user.username
        user.firstname = data.user.firstname
        user.lastname = data.user.lastname
        user.email = data.user.email
        user.password = springSecurityService.encodePassword(data.user.password)
        user.enabled = true
      }
      else if (params.format.toLowerCase() == "xml") {
        data = request.XML
        user.username = data.user.username.user.text()
        user.firstname = data.user.firstname.text()
        user.lastname = data.user.lastname.text()
        user.email = data.user.email.text()
        user.password = springSecurityService.encodePassword(data.user.password.text())
        user.enabled = true
      }

      if (user.validate()) {
        user.save();
        response.status = 201
        render ""
      } else {
        sendValidationFailedResponse(user, 403)
      }
    }


    def delete = {
       User user = User.get(params.id)
       if (!user) {
         SendNotFoundResponse()
        }
       SecUserSecRole.removeAll(user)
       user.delete();
       response.status = 204
       render ""
    }


    /* REST UTILITIES */
    private def SendNotFoundResponse() {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("User not found with id: " + params.id)
        }
      }
    }

    private def sendValidationFailedResponse(user, status) {
      response.status = status
      render contentType: "application/xml", {
        errors {
          user?.errors?.fieldErrors?.each {err ->
            field(err.field)
            message(g.message(error: err))
          }
        }
      }
    }
}
