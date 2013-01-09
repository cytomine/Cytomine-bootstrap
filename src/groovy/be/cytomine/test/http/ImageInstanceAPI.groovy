package be.cytomine.test.http

import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.ontology.ReviewedAnnotation

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageInstance to Cytomine with HTTP request during functional test
 */
class ImageInstanceAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json"
        return doGET(URL, username, password)
    }

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def create(String jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance.json"
        def result = doPOST(URL,jsonImageInstance,username,password)
        result.data = ImageInstance.get(JSON.parse(result.data)?.imageinstance?.id)
        return result
    }

    static def update(def id, def jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + ".json"
        return doPUT(URL,jsonImageInstance,username,password)
    }

    static def delete(ImageInstance image, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + image.id + ".json"
        return doDELETE(URL,username,password)
    }

    static ImageInstance buildBasicImage(String username, String password) {
        //Create project with user 1
        def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(), username, password)
        assert 200==result.code
        Project project = result.data
        //Add image with user 1
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        result = ImageInstanceAPI.create(image.encodeAsJSON(), username, password)
        assert 200==result.code
        image = result.data
        return image
    }
}
