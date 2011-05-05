package be.cytomine.command
import grails.converters.*
import be.cytomine.security.User

class TransactionController {

  def springSecurityService

    def begin = {
      log.info "begin transaction:" + springSecurityService.principal.id
      User user = User.get(springSecurityService.principal.id)
      user.setTransactionInProgress(true)
      user.transaction++;
      user.save(flush:true)
      log.info "save transac:" + user.transactionInProgress
      response.status = 200
      def data = [];
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    }

    def end = {
      log.info "end transaction:" + springSecurityService.principal.id
      User user = User.get(springSecurityService.principal.id)
      user.setTransactionInProgress(false)
      user.save(flush:true)
      log.info "save transac:" + user.transactionInProgress
      response.status = 200
      def data = [];
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }

    }
}
