package cytomine.web

import be.cytomine.security.SecUser
import be.cytomine.command.Transaction

class TransactionService {

  static transactional = true


  def next(SecUser user) {
    return new Transaction(user : user, inProgress : user.transactionInProgress)
    /*Transaction.withTransaction {
      def transaction = Transaction.findByDateEndAndUser(null, user)

      if (transaction == null) {
        transaction = new Transaction(user : user)
        transaction.save(flush : true)
      }
      else {
        //transaction.lock()
        if (user.transactionInProgress == false) {
          transaction.setDateEnd(new Date())
          transaction.save(flush : true)
          transaction = new Transaction(user : user)
          transaction.save()
        }
      }
      return transaction
    }*/

  }
}


