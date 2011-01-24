package be.cytomine.security

import be.cytomine.command.Transaction

class SecUser {

  String username
  String password
  boolean enabled
  boolean accountExpired
  boolean accountLocked
  boolean passwordExpired
  List transactions
  Boolean transactionInProgress = false //indicates whether the current user is doing several actions seen as only one action

  static transients = ["currentTransaction", "nextTransaction"]

  static hasMany = [userGroup:UserGroup, transactions:Transaction]


  static constraints = {
    username blank: false, unique: true
    password blank: false
  }

  static mapping = {
    password column: '`password`'
  }

  Set<SecRole> getAuthorities() {
    SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
  }

  private void setTransactionInProgress(Boolean transactionInProgress) {

    //prevent to begin a transaction more than one time
    if (this.transactionInProgress && transactionInProgress) return

    //prevent to end a transaction more than one time
    if (!this.transactionInProgress && !transactionInProgress) return

    def transaction = transactions.find { t -> (t.getDateEnd() == null)}
    if (transaction != null) {
      transaction.setDateEnd(new Date())
      transaction.save()
    }
    this.transactionInProgress = transactionInProgress
  }

  Transaction getCurrentTransaction() {
    return transactions.find { t -> (t.getDateEnd() == null)}
  }

  Transaction getNextTransaction() { //bad code here, DRY :(
    def transaction = transactions.find { t -> (t.getDateEnd() == null)}
    if (transaction == null) {
      //create new transaction and return it
      transaction = new Transaction (dateBegin: new Date(), dateEnd : null)
      addToTransactions(transaction)
      transaction.save()
      return transaction
    }

    if (!transactionInProgress) {
      //close previous transaction
      transaction.setDateEnd(new Date())
      transaction.save()
      //create new transaction
      transaction = new Transaction (dateBegin: new Date(), dateEnd : null)
      addToTransactions(transaction)
      transaction.save()
    }
    return transaction
  }
}
