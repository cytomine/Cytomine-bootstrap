package cytomine.web

import be.cytomine.image.UploadedFile
import be.cytomine.project.Slide
import be.cytomine.image.AbstractImage
import be.cytomine.image.Mime
import be.cytomine.security.Group
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.security.SecUser
import be.cytomine.image.ImageInstance
import grails.util.GrailsUtil

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 28/03/12
 * Time: 14:51
 */
class DeployImagesJob {

    def deployImagesService
    def cytomineService

    static triggers = {
         simple name: 'deployImagesJob', startDelay: 60000, repeatInterval: 1000*60*5 //5 minutes
    }

    void execute(context) {
        return
        //if (GrailsUtil.environment != "production") return
        println "START (DeployImagesJob)"
        UploadedFile.findAllByStatus(UploadedFile.CONVERTED).each { uploadedFile ->
            deployImagesService.deployUploadedFile(uploadedFile, cytomineService.getCurrentUser())
        }
        println "DONE (DeployImagesJob)"
    }
}
