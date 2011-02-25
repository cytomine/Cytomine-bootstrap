package be.cytomine.command

import be.cytomine.SequenceDomain

class Command extends SequenceDomain {

  def messageSource

  String data
  String postData

  static constraints = {
    data (type:'text', maxSize:10240, nullable : true)
    postData (type:'text', maxSize:10240)
  }

}
