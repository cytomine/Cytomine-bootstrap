package be.cytomine.command

class Command {

  Transaction transaction
  String data
  Date date = new Date()
  String postData

  static belongsTo = [transaction:Transaction]

  static constraints = {
    data (type:'text', maxSize:10240, nullable : true)
    postData (type:'text', maxSize:10240)
  }

}
