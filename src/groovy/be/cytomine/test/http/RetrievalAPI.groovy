package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class RetrievalAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def getResults(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/annotation/$id/retrieval.json"
        return doGET(URL, username, password)
    }

    static def index(Long id, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/annotation/$id/retrieval.json"
        return doGET(URL, username, password)
    }
}
