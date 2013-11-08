package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * An UploadedFile is a file uploaded through the API.
 * Uploaded are temporaly instances, files related to them are placed
 * in a buffer space before being converted into the right format and copied to the storages
 */
class UploadedFile extends CytomineDomain implements Serializable{

//    public static allowedMime = ["svs", "opt", "jp2", "scn"]
//    public static zipMime = ["zip"]
//    public static mimeToConvert = ["jpg", "jpeg", "png", "tiff", "tif","pgm"]//, "ndpi"]
    public static int UPLOADED = 0
    public static int CONVERTED = 1
    public static int DEPLOYED = 2
    public static int ERROR_FORMAT = 3
    public static int ERROR_CONVERT = 4
    public static int UNCOMPRESSED = 5
    public static int TO_DEPLOY = 6

    SecUser user
    Long[] projects //projects ids that we have to link with the new file
    Long[] storages //storage ids on which we have to upload files
    String filename
    String originalFilename
    String convertedFilename
    String convertedExt
    String ext
    String path
    String contentType
    UploadedFile parent
    Long size
    int status = 0

    /**
     * Indicates whether or not a conversion was done
     */
    Boolean converted = false

    static constraints = {
        projects nullable: true
        storages nullable: false
        convertedFilename (nullable: true)
        convertedExt (nullable: true)
        parent(nullable : true)
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + UploadedFile.class)
        JSON.registerObjectMarshaller(UploadedFile) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['id'] = it.id
            returnArray['user'] = it.user.id
            returnArray['projects'] = it.projects
            returnArray['storages'] = it.storages
            returnArray['filename'] = it.filename
            returnArray['originalFilename'] = it.originalFilename
            returnArray['ext'] = it.ext
            returnArray['contentType'] = it.contentType
            returnArray['size'] = it.size
            returnArray['path'] = it.path
            returnArray['status'] = it.status
            returnArray['uploaded'] = (it.status == UploadedFile.UPLOADED)
            returnArray['converted'] = (it.status == UploadedFile.CONVERTED)
            returnArray['deployed'] = (it.status == UploadedFile.DEPLOYED)
            returnArray['error_format'] = (it.status == UploadedFile.ERROR_FORMAT)
            returnArray['error_convert'] = (it.status == UploadedFile.ERROR_CONVERT)
            returnArray['uncompressed'] = (it.status == UploadedFile.UNCOMPRESSED)
            returnArray['to_deploy'] = (it.status == UploadedFile.TO_DEPLOY)
            return returnArray
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static UploadedFile insertDataIntoDomain(def json, def domain = new UploadedFile()) throws CytomineException {
        println json

        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.created = JSONUtils.getJSONAttrDate(json,'created')
        domain.updated = JSONUtils.getJSONAttrDate(json,'updated')

        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new User(), true)

        domain.projects = JSONUtils.getJSONAttrListLong(json,'projects')
        domain.storages = JSONUtils.getJSONAttrListLong(json,'storages')

        domain.filename = JSONUtils.getJSONAttrStr(json,'filename')
        domain.originalFilename = JSONUtils.getJSONAttrStr(json,'originalFilename')
        domain.convertedFilename = JSONUtils.getJSONAttrStr(json,'convertedFilename')
        domain.convertedExt = JSONUtils.getJSONAttrStr(json,'convertedExt')
        domain.ext = JSONUtils.getJSONAttrStr(json,'ext')
        domain.path = JSONUtils.getJSONAttrStr(json,'path')
        domain.contentType = JSONUtils.getJSONAttrStr(json,'contentType')

        domain.parent = JSONUtils.getJSONAttrDomain(json, "parent", new UploadedFile(), false)

        domain.size = JSONUtils.getJSONAttrLong(json,'size',0)

        domain.status = JSONUtils.getJSONAttrInteger(json,'status',0)

        domain.converted = JSONUtils.getJSONAttrBoolean(json,'converted',false)


        return domain;
    }




    def getAbsolutePath() {
        return [ this.path, this.filename].join(File.separator)
    }

}
