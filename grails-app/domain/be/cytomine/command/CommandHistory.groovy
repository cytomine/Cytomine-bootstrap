package be.cytomine.command
import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.SequenceDomain

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 23/05/11
 * Time: 9:16
 * To change this template use File | Settings | File Templates.
 */
class CommandHistory extends SequenceDomain{
  Command command
  String prefixAction = "" //undo, redo or nothing


  static void registerMarshaller() {
    println "Register custom JSON renderer for " + CommandHistory.class
    JSON.registerObjectMarshaller(CommandHistory) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['command'] = it.command
      returnArray['prefixAction'] = it.prefixAction

      returnArray['created'] = it.created? it.created.time.toString() : null
      returnArray['updated'] = it.updated? it.updated.time.toString() : null

      return returnArray
    }
  }

}
