package be.cytomine.command

import be.cytomine.security.User

class TransactionController {

    def begin = {
      User user = User.findByUsername("stevben")
      user.setTransactionInProgress(true)
    }

    def end = {
      User user = User.findByUsername("stevben")
      user.setTransactionInProgress(false)

    }
}
