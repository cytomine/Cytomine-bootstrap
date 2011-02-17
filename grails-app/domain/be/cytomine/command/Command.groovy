package be.cytomine.command

import be.cytomine.SequenceDomain
import be.cytomine.security.User

class Command extends SequenceDomain {

  String data
  String postData

  static constraints = {
    data (type:'text', maxSize:10240, nullable : true)
    postData (type:'text', maxSize:10240)
  }

}
