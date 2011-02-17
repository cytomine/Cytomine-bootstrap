package be.cytomine.api.security

import be.cytomine.security.User
import grails.converters.*
import be.cytomine.command.user.AddUserCommand
import be.cytomine.command.UndoStack
import be.cytomine.command.Transaction
import be.cytomine.command.Command
import be.cytomine.command.user.EditUserCommand
import be.cytomine.command.user.DeleteUserCommand

/**
 * Handle HTTP Requests for CRUD operations on the User domain class.
 */
class RestUserController {

  def springSecurityService
  def transactionService

  /**
   * Render and returns all Users into the specified format given in the request
   * @return all Users into the specified format
   */
  def list = {
    def data = [:]
    data.user = User.list()
    data.total = User.count()

    withFormat {
      json { render data as JSON }
      xml { render data as XML }
    }
  }

  /**
   * Render and return an User into the specified format given in the request
   * @param id the user identifier
   * @return user an User into the specified format
   */
  def show = {
    if(params.id && User.exists(params.id)) {
      def data = User.findById(params.id)
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("User not found with id: " + params.id)
        }
      }
    }
  }

  /**
   * Create a new User according to the parameters passed into the request.
   * If successful, the new user is rendered and returned into the specified format
   * given in the request. If not, validations errors messages are returned as a response.
   * @param data the data related to the new user
   * @return user the new User into the specified format
   */
  def save = {
    User currentUser = User.get(springSecurityService.principal.id)
    Command addUserCommand = new AddUserCommand(postData : request.JSON.toString())

    def result = addUserCommand.execute()

    if (result.status == 201) {
      addUserCommand.save()
      new UndoStack(command : addUserCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  /**
   * Edit an existing User according to the parameters passed into the request.
   * If successful, the user is rendered with its modifications and returned into the specified format
   * given in the request. If not, validations errors messages are returned as a response.
   * @param data the data related to the user
   * @return user the edited User into the specified format
   */
  def update = {
    User currentUser = User.get(springSecurityService.principal.id)
    Command editUserCommand = new EditUserCommand(postData : request.JSON.toString())

    def result = editUserCommand.execute()
    if (result.status == 200) {
      editUserCommand.save()
      new UndoStack(command : editUserCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  /**
   * Delete a user according to the identifier passed into the request.
   * @param id the identifier of the user to delete
   * @return the identifier of the deleted user
   */
  def delete =  {
    User currentUser = User.get(springSecurityService.principal.id)
    def postData = ([id : params.id]) as JSON
    def result = null

    if (params.id == springSecurityService.principal.id) {
      result = [data : [success : false, message : "The user can't delete herself"], status : 403]
    } else {
      Command deleteUserCommand = new DeleteUserCommand(postData : postData.toString())

      result = deleteUserCommand.execute()
      if (result.status == 204) {
        deleteUserCommand.save()
        new UndoStack(command : deleteUserCommand, user: currentUser, transactionInProgress:  currentUser.transactionInProgress).save()
      }
    }
    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}
