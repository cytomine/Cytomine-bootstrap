package be.cytomine.command

/**
 * Start/Stop transaction (group of command request)
 */
class TransactionService {
    def springSecurityService
    static transactional = true

    /**
     * Start a transaction and return it
     */
    Transaction start() {
        synchronized (this.getClass()) {
            log.info "begin transaction:" + springSecurityService.principal.id
            //A transaction is a simple domain with a id (= transaction id)
            Transaction transaction = new Transaction()
            transaction.save(flush:true)
            return transaction
        }
    }
}
