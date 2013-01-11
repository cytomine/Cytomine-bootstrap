package be.cytomine.social

import be.cytomine.CytomineDomain
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.apache.log4j.Logger

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

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
     static void registerMarshaller(String cytomineBaseUrl) {
         Logger.getLogger(this).info("Register custom JSON renderer for " + UserPosition.class)
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
