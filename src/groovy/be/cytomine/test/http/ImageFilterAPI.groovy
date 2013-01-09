package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.processing.ImageFilter
import be.cytomine.processing.ImageFilterProject

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageFilter to Cytomine with HTTP request during functional test
 */
class ImageFilterAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagefilter/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imagefilter.json"
        return doGET(URL, username, password)
    }
}
