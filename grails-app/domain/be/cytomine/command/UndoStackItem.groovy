package be.cytomine.command

import be.cytomine.security.User
import be.cytomine.SequenceDomain

class UndoStackItem extends SequenceDomain implements Comparable {
  User user
  Command command
  Boolean transactionInProgress
  int transaction
  boolean isFromRedo = false //the undo item come from redo stack

  static belongsTo = [user:User, command:Command]

  int compareTo(obj) {
    created.compareTo(obj.created)
  }

  static constraints = {
    isFromRedo(nullable : true)
  }

  String toString() { return "|user="+user.id + " command="+command + " transaction="+transactionInProgress + " isFromRedo="+isFromRedo}
}
