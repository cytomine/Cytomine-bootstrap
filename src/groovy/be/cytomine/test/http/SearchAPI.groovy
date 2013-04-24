package be.cytomine.test.http

import be.cytomine.test.Infos
import be.cytomine.utils.SearchEnum.Filter
import be.cytomine.utils.SearchEnum.Operator

class SearchAPI extends DomainAPI {

    //LIST - Project ; Annotation ; Image
    static def listDomain(String keywords, Operator operator, Filter filter, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search?keywords=$keywords&operator=$operator&filter=$filter"
        return doGET(URL, username, password)
    }
}
