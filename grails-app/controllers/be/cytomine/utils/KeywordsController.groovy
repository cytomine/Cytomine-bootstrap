package be.cytomine.utils

import be.cytomine.api.RestController
import groovy.sql.Sql
/**
 * A Keywords a Cytomine user text entry that may be suggest in the futur.
 * If a user encode a new keywords or a new value "te...", we may use Keywords to retrieve all item with "te" (test, tel,...)
 *
 */
class KeywordsController extends RestController {

    def dataSource

    def list = {
        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow("select key from keyword order by key asc",[]) {
            data << it.key
        }
        try {
            sql.close()
        }catch (Exception e) {}
        responseSuccess(data)
    }
}
