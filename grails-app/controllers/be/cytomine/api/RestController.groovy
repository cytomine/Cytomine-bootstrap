package be.cytomine.api
import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoStackItem
import be.cytomine.command.EditCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Command
import be.cytomine.command.CommandHistory

class RestController {

  def springSecurityService
  int idUser

  static int SUCCESS_ADD_CODE = 200
  static int SUCCESS_EDIT_CODE = 200
  static int SUCCESS_DELETE_CODE = 200

  static int NOT_FOUND_CODE = 404
  static int TOO_LONG_REQUEST = 413

  User getCurrentUser(idUser) {
    log.info "User=" + idUser
    return User.read(idUser)
  }

  def processCommand(AddCommand c, User user)
  {
    processCommand(c,user,SUCCESS_ADD_CODE)
  }

  def processCommand(EditCommand c, User user)
  {
    processCommand(c,user,SUCCESS_EDIT_CODE)
  }

  def processCommand(DeleteCommand c, User user)
  {
    processCommand(c,user,SUCCESS_DELETE_CODE)
  }

  def processCommand(Command c, User user, int successCode) {
    def result
    log.info "c.postData.size()=" + c.postData.size() + " Command.MAXSIZEREQUEST=" + Command.MAXSIZEREQUEST
    if(c.postData.size()>=Command.MAXSIZEREQUEST) {
         response.status = TOO_LONG_REQUEST
        log.error "Request too long: " +  c.postData.size() + "character (max="+ Command.MAXSIZEREQUEST+")"
         return [object : null , errors : ["Request too long:"+c.postData.size() + "character"]]
    }

    result = c.execute()
    if (result.status == successCode) {
      log.info "c.project=" +c.project
      c.save()
      CommandHistory ch = new CommandHistory(command:c,prefixAction:"", project: c.project)

      ch.save();
      if(c.saveOnUndoRedoStack) {
        new UndoStackItem(command : c, user: user, transactionInProgress:  user.transactionInProgress, transaction : user.transaction).save(flush:true)
      }
    }
    log.debug "Lastcommands="+UndoStackItem.findAllByUser(user)
    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    return result.data
  }
  def response(data) {
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }
  def responseSuccess(data, code) {
    response.status = code
    response(data)
  }
  def responseSuccess(data) {
    response(data)
  }
  def responseNotFound(className,id) {
    log.error className + " Id " + id+ " don't exist"
    response.status = NOT_FOUND_CODE
    render(contentType:'text/json'){
      errors(message:className+" not found with id : " +id)
    }
  }

  def responseNotFound(className,filter,id) {
    log.error className + ": " + filter + " " + id+ " don't exist"
    response.status = NOT_FOUND_CODE
    render(contentType:'text/json'){
      errors(message:className+" not found with id " + filter + " : " +id)
    }
  }

  def responseNotFound(className,filter1,filter2,id1,id2) {
    log.error className + ": " + filter1 + " " + id1 + ", "+ filter2 + " " + id2+ " don't exist"
    response.status = NOT_FOUND_CODE
    render(contentType:'text/json'){
      errors(message:className+" not found with id " + filter1 + " : " +id1 + " and  " + filter2 + " : " +id2)
    }
  }

  def responseNotFound(className,filter1,id1,filter2,id2,filter3,id3) {
    log.error className + ": " + filter1 + " " + id1 + ", "+ filter2 + " " + id2+ " and " + filter3 + " " + id3+ " don't exist"
    response.status = NOT_FOUND_CODE
    render(contentType:'text/json'){
      errors(message:className+" not found with id " + filter1 + " : " +id1 + ",  " + filter2 + " : " +id2+ " and "+ filter3 + " : " +id3)
    }
  }

  def responseImage(String url) {
      def out = new ByteArrayOutputStream()
      out << new URL(url).openStream()
      response.contentLength = out.size();
      withFormat {
        jpg {
          if (request.method == 'HEAD') {
            render(text: "", contentType: "image/jpeg");
          }
          else {
            response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
          }
        }
      }
  }

}
