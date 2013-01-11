package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.apache.log4j.Logger

class UploadedFile extends CytomineDomain implements Serializable{

    public static allowedMime = ["vms", "mrxs", "svs", "opt", "jp2"]
    public static mimeToConvert = ["jpg", "png", "tiff", "tif","pgm"]//, "ndpi"]
    public static int UPLOADED = 0
    public static int CONVERTED = 1
    public static int DEPLOYED = 2
    public static int ERROR_FORMAT = 3

    SecUser user
    Project project
    String filename
    String originalFilename
    String convertedFilename
    String convertedExt
    String ext
    String path
    String contentType
    int size
    int status = 0

    static constraints = {
        project (nullable : true)
        convertedFilename (nullable: true)
        convertedExt (nullable: true)
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
     static void registerMarshaller(String cytomineBaseUrl) {
         Logger.getLogger(this).info("Register custom JSON renderer for " + UploadedFile.class)
        JSON.registerObjectMarshaller(UploadedFile) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['id'] = it.id
            returnArray['user'] = it.user.id
            returnArray['project'] = it.project?.id
            returnArray['filename'] = it.filename
            returnArray['originalFilename'] = it.originalFilename
            returnArray['ext'] = it.ext
            returnArray['contentType'] = it.contentType
            returnArray['size'] = it.size
            returnArray['uploaded'] = (it.status == UploadedFile.UPLOADED)
            returnArray['converted'] = (it.status == UploadedFile.CONVERTED)
            returnArray['deployed'] = (it.status == UploadedFile.DEPLOYED)
            returnArray['error'] = (it.status == UploadedFile.ERROR_FORMAT)
            return returnArray
        }
    }
}
