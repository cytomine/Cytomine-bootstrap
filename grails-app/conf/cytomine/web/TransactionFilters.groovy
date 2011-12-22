package cytomine.web

class TransactionFilters {

    def filters = {
        all(uri:'/api/**') {
          before = {
          }
          after = {
              //new TransactionController().stopIfTransactionInProgress()
          }
          afterView = {
          }
        }
    }
    
}
