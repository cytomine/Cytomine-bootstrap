package be.cytomine.command

import be.cytomine.command.Command
import be.cytomine.security.User
import be.cytomine.SequenceDomain


class UndoStack extends SequenceDomain {
  User user
  Command command

  static belongsTo = [user:User, command:Command]
}
