package be.cytomine.command

import be.cytomine.command.Command
import be.cytomine.security.User


class UndoStack {
  User user
  Command command

  static belongsTo = [user:User, command:Command]
}
