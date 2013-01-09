package be.cytomine.test.http

import be.cytomine.AnnotationDomain
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.project.Project
import be.cytomine.command.Task

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class TaskAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/task/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def create(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/task.json"
        def result = doPOST(URL,"{project:${idProject}}",username,password)
        return result
    }
}
