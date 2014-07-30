package be.cytomine.test.http

import be.cytomine.test.Infos

class SearchAPI extends DomainAPI {

    //LIST - Project ; Annotation ; Image
    static def listDomain(String keywords, String operator, String filter, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search.json?${keywords? "keywords=$keywords" : ""}&operator=$operator" + (filter? "&filter=$filter": "")
        return doGET(URL, username, password)
    }

    //v2
    static def search(List<String> words, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search-engine.json?&expr=${words.collect{URLEncoder.encode(it, "UTF-8")}.join(",")}"
        return doGET(URL, username, password)
    }
    static def searchResults(List<Long> ids, List<String> words, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search-result.json?&expr=${words.collect{URLEncoder.encode(it, "UTF-8")}.join(",")}&ids=${ids.join(",")}"
        return doGET(URL, username, password)
    }
}
