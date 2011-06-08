package be.cytomine.command
import grails.converters.JSON
import be.cytomine.SequenceDomain
import be.cytomine.security.User

class Command extends SequenceDomain {

  def messageSource

  String data
  String postData

  User user

  static Integer MAXSIZEREQUEST = 102400

  String actionMessage

  boolean saveOnUndoRedoStack = false //by default, don't save command on stack

  static constraints = {
    data (type:'text', maxSize:Command.MAXSIZEREQUEST, nullable : true)
    postData (type:'text', maxSize:Command.MAXSIZEREQUEST)
    actionMessage(nullable : true)
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Command.class
    JSON.registerObjectMarshaller(Command) {
      def returnArray = [:]

      returnArray['CLASSNAME'] = it.class
      returnArray['action'] = it.getActionMessage()
      returnArray['data'] = it.data
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

  public String toString() {
    return this.id + "["+this.created+"]";
  }

  /**
   * Get the class name of an object without package name
   * @param o Object
   * @return Class name (without package) of o
   */
  protected String getClassName(Object o) {
    log.info("getClassName="+o.getClass());
    String name = o.getClass()   //be.cytomine.image.Image
    String[] array =  name.split("\\.")  //[be,cytomine,image,Image]
    log.info array.length
    return array[array.length-1] // Image
  }

}
