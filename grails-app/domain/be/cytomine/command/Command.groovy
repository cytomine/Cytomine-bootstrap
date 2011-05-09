package be.cytomine.command
import grails.converters.JSON
import be.cytomine.SequenceDomain
import be.cytomine.security.User

class Command extends SequenceDomain {

  def messageSource

  String data
  String postData

  User user

  //String actiontype //add, delete or update
  //String objectType = "Unknown"
  String actionMessage

  boolean saveOnUndoRedoStack = false //by default, don't save command on stack

  static constraints = {
    data (type:'text', maxSize:10240, nullable : true)
    postData (type:'text', maxSize:10240)
    actionMessage(nullable : true)
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Command.class
    JSON.registerObjectMarshaller(Command) {
      def returnArray = [:]

      returnArray['class'] = it.class
      returnArray['action'] = it.getActionMessage()

      returnArray['type'] = "UNKNOWN"
      if(it instanceof AddCommand) returnArray['type'] = "ADD"
      else if(it instanceof EditCommand) returnArray['type'] = "EDIT"
      else if(it instanceof DeleteCommand) returnArray['type'] = "DELETE"


      returnArray['created'] = it.created? it.created.time.toString() : null
      returnArray['updated'] = it.updated? it.updated.time.toString() : null

      return returnArray
    }
  }

 String getActionMessage()
  {
    return  actionMessage
  }

}
