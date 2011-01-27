package be.cytomine.api.security

import be.cytomine.security.User
import grails.converters.*
import be.cytomine.security.SecUserSecRole
import be.cytomine.command.AddUserCommand
import be.cytomine.command.stack.UndoStack
import be.cytomine.command.Transaction
import be.cytomine.command.Command
import be.cytomine.command.EditUserCommand
import be.cytomine.security.Group
import be.cytomine.security.UserGroup
import be.cytomine.command.DeleteUserCommand

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 6/01/11
 * Time: 20:27
 */
class RestUserController {

  def springSecurityService


  def index = {
    redirect(controller : "user")
  }

  /* REST API */

  def list = {
    def data = [:]
    data.user = User.list()

    withFormat {
      json { render data as JSON }
      xml { render data as XML }
    }
  }

  def show = {
    if(params.id && User.exists(params.id)) {
      def data = User.findById(params.id)
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      SendNotFoundResponse()
    }
  }

  def save = {
    User user = User.findById(3)
    Command addUserCommand = new AddUserCommand(postData : request.JSON.toString())
    Transaction currentTransaction = user.getNextTransaction()
    currentTransaction.addToCommands(addUserCommand)
    def result = addUserCommand.execute()

    if (result.status == 201) {
      addUserCommand.save()
      new UndoStack(command : addUserCommand, user: user).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }


  def update = {
    User user = User.findById(3)
    Command editUserCommand = new EditUserCommand(postData : request.JSON.toString())
    Transaction currentTransaction = user.getNextTransaction()
    currentTransaction.addToCommands(editUserCommand)
    def result = editUserCommand.execute()

    if (result.status == 200) {
      if (!editUserCommand.validate()) {
        sendValidationFailedResponse(editUserCommand, 403)
      }
      editUserCommand.save()
      new UndoStack(command : editUserCommand, user: user).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def delete =  {
    User user = User.findById(3)
    def postData = ([id : params.id]) as JSON
    def result = null

    if (params.id == "3") {
      result = [data : [success : false, message : "The user can't delete herself"], status : 403]
    } else {
      Command deleteUserCommand = new DeleteUserCommand(postData : postData.toString())
      Transaction currentTransaction = user.getNextTransaction()
      currentTransaction.addToCommands(deleteUserCommand)
      result = deleteUserCommand.execute()

      if (result.status == 204) {
        deleteUserCommand.save()
        new UndoStack(command : deleteUserCommand, user: user).save()
      }
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
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

  private def sendSuccessResponse(user, status) {

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
