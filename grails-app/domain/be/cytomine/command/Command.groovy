package be.cytomine.command

import be.cytomine.SequenceDomain
import be.cytomine.security.User

class Command extends SequenceDomain {

  def messageSource

  String data
  String postData

  User user

  boolean saveOnUndoRedoStack = false //by default, don't save command on stack

  static constraints = {
    data (type:'text', maxSize:10240, nullable : true)
    postData (type:'text', maxSize:10240)
  }

}
