package be.cytomine.social

import be.cytomine.CytomineDomain
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 24/02/12
 * Time: 16:39
 */
class UserPosition extends CytomineDomain {

    static belongsTo = [user : SecUser, image : ImageInstance, project: Project]

    double longitude
    double latitude
    int zoom

    static constraints = {
        project nullable: true
    }

     static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + UserPosition.class
        JSON.registerObjectMarshaller(UserPosition) {
            def returnArray = [:]
            returnArray.id = it.id
            returnArray.date = it.updated != null ? it.updated : it.created
            returnArray.user = it.user.id
            returnArray.image = it.image.id
            returnArray.project = it.project.id
            returnArray.zoom = it.zoom
            returnArray.latitude = it.latitude
            returnArray.longitude = it.longitude
            return returnArray
        }
    }
}
