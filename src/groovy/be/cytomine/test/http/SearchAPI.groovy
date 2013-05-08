package be.cytomine.test.http

import be.cytomine.test.Infos
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator


class SearchAPI extends DomainAPI {

    //LIST - Project ; Annotation ; Image
    static def listDomain(String keywords, SearchOperator operator, SearchFilter filter, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search?keywords=$keywords&operator=$operator&filter=$filter"
        return doGET(URL, username, password)
    }
}
