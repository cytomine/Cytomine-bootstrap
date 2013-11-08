package be.cytomine.test.http

import be.cytomine.test.Infos

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage User to Cytomine with HTTP request during functional test
 */
class AclAPI extends DomainAPI {

//
//    "/api/domain/$domainClassName/$domainIdent/user/$user"(controller:"restACL"){
//        action = [GET:"list",POST:"add",DELETE: "delete"]
//    }

    static def list(String domainClassName, Long domainIdent, Long user, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/$domainClassName/$domainIdent/user/${(user? user : "")}"
        return doGET(URL, username, password)
    }

    static def create(String domainClassName, Long domainIdent, Long user, String auth, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/$domainClassName/$domainIdent/user/$user?" + (auth? "auth=$auth" : "")
        def result = doPOST(URL,"",username,password)
        return result
    }

    static def delete(String domainClassName, Long domainIdent, Long user, String auth, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/$domainClassName/$domainIdent/user/$user?" + (auth? "auth=$auth" : "")
        return doDELETE(URL,username,password)
    }
}
