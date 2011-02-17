package be.cytomine.command

import be.cytomine.SequenceDomain

class Transaction extends SequenceDomain {

  Date dateBegin
  Date dateEnd

  List commands

  static hasMany = [commands:Command]

  def beforeInsert() {
    super.beforeInsert()
    dateBegin = new Date()
    dateEnd = null
  }

  def beforeUpdate() {
    super.beforeUpdate()
  }

  static constraints = {
    dateEnd nullable : true
  }


}
