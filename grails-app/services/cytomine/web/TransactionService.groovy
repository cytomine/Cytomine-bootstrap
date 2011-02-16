package cytomine.web

import be.cytomine.security.SecUser
import be.cytomine.command.Transaction

class TransactionService {

  static transactional = true

  private def createTransaction(SecUser user) {
    def newTransaction = new Transaction()
    user.addToTransactions(newTransaction)
    newTransaction.save()
  }

  def next(SecUser user) {


    if (user.transactions.size() == 0) {
      createTransaction(user)
    }

    if (!user.transactionInProgress) {
      Transaction lastTransaction = user.transactions.last()
      lastTransaction.setDateEnd(new Date())
      user.save()

      createTransaction(user)
    }

    return user.transactions.last()
  }

}
