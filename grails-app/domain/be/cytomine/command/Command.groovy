package be.cytomine.command

class Command {

  Transaction transaction
  String data
  Date date = new Date()
  String postData

  static belongsTo = Transaction

  static constraints = {
    data (type:'text', maxSize:2048, nullable : true)
    postData (type:'text', maxSize:2048)
  }

}
