package cytomine.web

import be.cytomine.image.UploadedFile


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
         //simple name: 'deployImagesJob', startDelay: 60000, repeatInterval: 1000*60*5 //5 minutes
    }

    void execute(context) {
        UploadedFile.findAllByStatus(UploadedFile.CONVERTED).each { uploadedFile ->
            deployImagesService.deployUploadedFile(uploadedFile, cytomineService.getCurrentUser())
        }

    }
}
