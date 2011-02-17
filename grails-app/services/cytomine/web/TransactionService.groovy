package cytomine.web

import be.cytomine.security.SecUser
import be.cytomine.command.Transaction

class TransactionService {

  static transactional = false

  private def createTransaction(SecUser user) {

    def newTransaction = new Transaction()
    user.addToTransactions(newTransaction)
    user.save()
    return newTransaction
  }

  def next(SecUser user) {

    if (user.transactions.size() == 0) {
      return createTransaction(user)
    }

    if (!user.transactionInProgress) {
      /*Transaction lastTransaction = user.transactions.last()
      lastTransaction.lock()
      lastTransaction.setDateEnd(new Date())
      lastTransaction.save()*/

      return createTransaction(user)

    } else {
      return user.transactions.last()
    }
  }

}
