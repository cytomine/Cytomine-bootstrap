package cytomine.web

import be.cytomine.security.SecUser
import be.cytomine.command.Transaction

class TransactionService {

  static transactional = true

  def next(SecUser user) {
    def transaction = Transaction.findByDateEndAndUser(null, user)
    if (transaction == null) {
      transaction = new Transaction(user : user)
      transaction.save()
    }
    else {
      transaction.lock()
      if (user.transactionInProgress == false) {
        transaction.setDateEnd(new Date())
        transaction.save()
        transaction = new Transaction(user : user)
        transaction.save()
      }
    }
    return transaction
  }

}
