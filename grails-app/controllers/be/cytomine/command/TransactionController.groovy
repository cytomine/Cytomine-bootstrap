package be.cytomine.command
import grails.converters.*
import be.cytomine.security.User
import be.cytomine.security.SecUser

class TransactionController {

    def springSecurityService


    def start() {
        synchronized(this.getClass()) {
            log.info "begin transaction:" + springSecurityService.principal.id
            User user = User.get(springSecurityService.principal.id)
            user.setTransactionInProgress(true)
            user.transaction++;
            user.save(flush:true)
        }
    }

    def stop() {
        synchronized(this.getClass()) {
            log.info "end transaction:" + springSecurityService.principal.id
            stop(User.get(springSecurityService.principal.id))
        }
    }

    def stopIfTransactionInProgress() {
        synchronized(this.getClass()) {
            //log.info "end transaction:" + springSecurityService.principal.id
            SecUser user = User.get(springSecurityService.principal.id)
            if(user.transactionInProgress) {
                stop(user)
            }
        }
    }

    def stop(SecUser user){
        synchronized(this.getClass()) {
            user.setTransactionInProgress(false)
            user.save(flush:true)
        }
    }
}
