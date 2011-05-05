package be.cytomine.command

import be.cytomine.security.User
import be.cytomine.SequenceDomain

class RedoStackItem extends SequenceDomain implements Comparable {
  User user
  Command command
  Boolean transactionInProgress
  int transaction

  static belongsTo = [user:User, command:Command]

  int compareTo(obj) {
    created.compareTo(obj.created)
  }
}
