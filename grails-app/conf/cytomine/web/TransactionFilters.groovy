package cytomine.web

import be.cytomine.command.TransactionController

class TransactionFilters {

    def filters = {
        all(uri:'/api/**') {
          before = {
          }
          after = {
              new TransactionController().stopIfTransactionInProgress()
          }
          afterView = {
          }
        }
    }
    
}
