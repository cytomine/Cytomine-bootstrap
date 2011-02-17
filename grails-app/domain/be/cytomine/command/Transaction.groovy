package be.cytomine.command

import be.cytomine.SequenceDomain
import be.cytomine.security.User

class Transaction extends SequenceDomain {

  Date dateEnd
  User user
  Boolean inProgress

  static constraints = {
    dateEnd nullable : true
  }


}
