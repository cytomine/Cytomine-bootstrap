package be.cytomine

import be.cytomine.command.AddCommand
import org.codehaus.groovy.grails.web.json.JSONElement
import java.util.prefs.BackingStoreException
import java.sql.SQLException
import be.cytomine.command.EditCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Command
import be.cytomine.command.CommandHistory
import be.cytomine.security.User
import be.cytomine.command.UndoStackItem
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.TooLongRequestException

class CommandService {

  def springSecurityService
  int idUser

  static int SUCCESS_ADD_CODE = 200
  static int SUCCESS_EDIT_CODE = 200
  static int SUCCESS_DELETE_CODE = 200

  static int NOT_FOUND_CODE = 404
  static int TOO_LONG_REQUEST = 413


  def processCommand(AddCommand c,JSONElement json)  throws CytomineException
  {
    processCommand(c,json,SUCCESS_ADD_CODE)
  }

  def processCommand(EditCommand c, JSONElement json)  throws CytomineException
  {
    processCommand(c,json,SUCCESS_EDIT_CODE)
  }

  def processCommand(DeleteCommand c,JSONElement json)   throws CytomineException
  {
    processCommand(c,json,SUCCESS_DELETE_CODE)
  }

  def processCommand(Command c, JSONElement json, int successCode) throws CytomineException{
    log.info "processCommand:"+json
    c.setJson(json)
    c.postData = json.toString()

    log.debug "c.postData.size()=" + c.postData.size() + " Command.MAXSIZEREQUEST=" + Command.MAXSIZEREQUEST
    if(c.postData.size()>=Command.MAXSIZEREQUEST) {
        throw new TooLongRequestException("Request is too long")
    }
    def result = c.execute()
    if (result.status == successCode) {
      c.save()
      CommandHistory ch = new CommandHistory(command:c,prefixAction:"", project: c.project)
      ch.save();
      if(c.saveOnUndoRedoStack) {
        User user = c.user
        new UndoStackItem(command : c, user: user, transactionInProgress:  user.transactionInProgress, transaction : user.transaction).save(flush:true)
      }
    }

    log.debug "result.status="+result.status+" result.data=" + result.data
      //result = (status: + data:)
    return result
  }
}
