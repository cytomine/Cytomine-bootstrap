package be.cytomine.command

import be.cytomine.security.User

class Transaction {

  Date dateBegin = new Date()
  Date dateEnd = null

  List commands

  static hasMany = [commands:Command]


  static constraints = {
    dateEnd nullable : true
  }


}
