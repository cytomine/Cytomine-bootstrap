package be.cytomine.command

import be.cytomine.security.User

class AddUserCommand extends UndoRedoCommand {

  Long idUser

  static constraints = {
    data maxSize: 1000
  }

  def execute() {
    def newUser = User.getUserFromData(data)
    if (newUser.validate()) {
      newUser.save()
      idUser = newUser.id
      return [data : newUser, status : 201]
    } else {
      return [data : newUser, status : 403]
    }
  }

  def undo() {
    def user = User.findById(idUser)
    user.delete()
    return [data : null, status : 200]
  }

  def redo() {
    def newUser = User.getUserFromData(data)
    newUser.save()
    idUser = newUser.id //TO DO :problem here !!! we break command which had references to this object
    this.save()
    return [data : newUser, status : 200]
  }
}
