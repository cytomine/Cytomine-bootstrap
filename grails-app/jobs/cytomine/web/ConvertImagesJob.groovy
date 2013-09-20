package cytomine.web

import be.cytomine.image.UploadedFile

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 27/03/12
 * Time: 14:58
 */
class ConvertImagesJob {

//    def convertImagesService
    def cytomineService

    static triggers = {
        //simple name: 'convertImagesJob', startDelay: 60000, repeatInterval: 1000*60*5 //5 minutes
    }

    void execute(context) {
//        UploadedFile.findAllByStatus(UploadedFile.UPLOADED).each { uploadedFile ->
//           convertImagesService.convertUploadedFile(uploadedFile, cytomineService.getCurrentUser())
//        }
    }

}
