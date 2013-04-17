package be.cytomine.test.http

import be.cytomine.AnnotationDomain
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class AnnotationIndexAPI extends DomainAPI {

    static def listByImage(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/annotationindex.json"
        return doGET(URL, username, password)
    }

}
