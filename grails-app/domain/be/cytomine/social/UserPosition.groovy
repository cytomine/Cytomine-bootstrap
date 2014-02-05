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
 *
 *  User position on an image at a time
 *  Usefull to allow user following
 */
class UserPosition extends CytomineDomain {

    static belongsTo = [user : SecUser, image : ImageInstance, project: Project]

    /**
     * User position on image
     */
    double longitude
    double latitude

    /**
     * User zoom on image
     */
    int zoom

    static constraints = {
        project nullable: true
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray.date = domain?.updated != null ? domain?.updated : domain?.created
        returnArray.user = domain?.user?.id
        returnArray.image = domain?.image?.id
        returnArray.project = domain?.project?.id
        returnArray.zoom = domain?.zoom
        returnArray.latitude = domain?.latitude
        returnArray.longitude = domain?.longitude
        returnArray
    }
}
