package be.cytomine.command

import be.cytomine.SequenceDomain

class Transaction extends SequenceDomain {

  Date dateEnd

  List commands

  static hasMany = [commands:Command]

  static constraints = {
    dateEnd nullable : true
  }


}
