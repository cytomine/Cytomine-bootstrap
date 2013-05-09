package be.cytomine.test.http

import be.cytomine.test.Infos

class SearchAPI extends DomainAPI {

    //LIST - Project ; Annotation ; Image
    static def listDomain(String keywords, String operator, String filter, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search?keywords=$keywords&operator=$operator&filter=$filter"
        return doGET(URL, username, password)
    }
}
