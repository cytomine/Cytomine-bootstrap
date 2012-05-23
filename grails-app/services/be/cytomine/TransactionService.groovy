package be.cytomine

import be.cytomine.command.Transaction

class TransactionService {
    def springSecurityService
    static transactional = true

    Transaction start() {
        synchronized (this.getClass()) {
            log.info "begin transaction:" + springSecurityService.principal.id
            Transaction transaction = new Transaction()
            transaction.save(flush:true)
            log.info "Transaction ${transaction.id} saved"
            return transaction
        }
    }

    def stop() {
       log.info "end transaction:" + springSecurityService.principal.id
    }
}
