package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 2013/10/07
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AttachedFile to Cytomine with HTTP request during functional test
 */
class AttachedFileAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/attachedfile/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/attachedfile.json"
        return doGET(URL, username, password)
    }

    static def listByDomain(String domainClassName, Long domainIdent, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/$domainClassName/$domainIdent/attachedfile.json"
        return doGET(URL, username, password)
    }

    static def download(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/attachedfile/${id}.json"
        return doGET(URL, username, password)
    }

    static def upload(String domainClassName, Long domainIdent, File file, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/attachedfile.json?domainClassName=$domainClassName&domainIdent=$domainIdent"
        return doPOSTUpload(URL,file,username,password)
    }
}
