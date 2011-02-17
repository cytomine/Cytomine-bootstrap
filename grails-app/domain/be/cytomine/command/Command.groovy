package be.cytomine.command

import be.cytomine.SequenceDomain

class Command extends SequenceDomain {

  Transaction transaction
  String data
  String postData

  static belongsTo = [transaction:Transaction]

  static constraints = {
    data (type:'text', maxSize:10240, nullable : true)
    postData (type:'text', maxSize:10240)
  }

}
