package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.ontology.AnnotationFilter

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class AnnotationFilterAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationfilter/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationfilter.json?project=$id"
        return doGET(URL, username, password)
    }

    static def listByOntology(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/$id/annotationfilter.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationfilter.json"
        def result = doPOST(URL, json,username, password)
        Long idAnnotationFilter = JSON.parse(result.data)?.annotationfilter?.id
        return [data: AnnotationFilter.get(idAnnotationFilter), code: result.code]
    }

    static def update(def id, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationfilter/" + id + ".json"
        return doPUT(URL,json,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationfilter/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
