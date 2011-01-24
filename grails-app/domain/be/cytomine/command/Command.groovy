package be.cytomine.command

class Command {

  Transaction transaction
  String data
  Date date = new Date()

  static belongsTo = Transaction

  static constraints = {
    data maxSize:1024
  }

  def execute() {}
}
