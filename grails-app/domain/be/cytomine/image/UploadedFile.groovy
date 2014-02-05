package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.api.UrlApi
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * An UploadedFile is a file uploaded through the API.
 * Uploaded are temporaly instances, files related to them are placed
 * in a buffer space before being converted into the right format and copied to the storages
 */
@ApiObject(name = "uploaded file", description = "A file uploaded on the server, when finished, we create an 'abstract image' from this uploaded file")
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

    @ApiObjectFieldLight(description = "The user that upload the file")
    SecUser user

    @ApiObjectFieldLight(description = "List of project (id) that will get the image")
    Long[] projects //projects ids that we have to link with the new file

    @ApiObjectFieldLight(description = "List of storage (id) that will store the image")
    Long[] storages //storage ids on which we have to upload files

    @ApiObjectFieldLight(description = "Upload file filename")
    String filename

    @ApiObjectFieldLight(description = "Upload file short name")
    String originalFilename

    @ApiObjectFieldLight(description = "The converted filename", presentInResponse = false)
    String convertedFilename

    @ApiObjectFieldLight(description = "The converted extension", presentInResponse = false)
    String convertedExt

    @ApiObjectFieldLight(description = "Extension name")
    String ext

    @ApiObjectFieldLight(description = "Path name")
    String path

    @ApiObjectFieldLight(description = "File content type", presentInResponse = false)
    String contentType


    UploadedFile parent

    @ApiObjectFieldLight(description = "File size", mandatory = false)
    Long size

    @ApiObjectFieldLight(description = "File status (UPLOADED = 0,CONVERTED = 1,DEPLOYED = 2,ERROR_FORMAT = 3,ERROR_CONVERT = 4,UNCOMPRESSED = 5,TO_DEPLOY = 6)", mandatory = false)
    int status = 0

    /**
     * Indicates whether or not a conversion was done
     */
    @ApiObjectFieldLight(description = "Indicates wether or not a conversion was done", mandatory = false)
    Boolean converted = false

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "uploaded", description = "Indicates if the file is uploaded",allowedType = "boolean",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "converted", description = "Indicates if the file is converted",allowedType = "boolean",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "deployed", description = "Indicates if the file is deployed",allowedType = "boolean",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "error_format", description = "Indicates if there is a error with file format",allowedType = "boolean",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "error_convert", description = "Indicates if there is an error with file conversion",allowedType = "boolean",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "uncompressed", description = "Indicates if the file is not compressed",allowedType = "boolean",useForCreation = false)
    ])
    static transients = ["zoomLevels", "thumbURL"]



    static constraints = {
        projects nullable: true
        storages nullable: false
        convertedFilename (nullable: true)
        convertedExt (nullable: true)
        parent(nullable : true)
    }

    static def getDataFromDomain(def uploaded) {
        def returnArray = CytomineDomain.getDataFromDomain(uploaded)
        println uploaded
        returnArray['user'] = uploaded?.user?.id
        returnArray['projects'] = uploaded?.projects
        returnArray['storages'] = uploaded?.storages
        returnArray['filename'] = uploaded?.filename
        returnArray['originalFilename'] = uploaded?.originalFilename
        returnArray['ext'] = uploaded?.ext
        returnArray['contentType'] = uploaded?.contentType
        returnArray['size'] = uploaded?.size
        returnArray['path'] = uploaded?.path
        returnArray['status'] = uploaded?.status
        returnArray['uploaded'] = (uploaded?.status == UploadedFile.UPLOADED)
        returnArray['converted'] = (uploaded?.status == UploadedFile.CONVERTED)
        returnArray['deployed'] = (uploaded?.status == UploadedFile.DEPLOYED)
        returnArray['error_format'] = (uploaded?.status == UploadedFile.ERROR_FORMAT)
        returnArray['error_convert'] = (uploaded?.status == UploadedFile.ERROR_CONVERT)
        returnArray['uncompressed'] = (uploaded?.status == UploadedFile.UNCOMPRESSED)
        returnArray['to_deploy'] = (uploaded?.status == UploadedFile.TO_DEPLOY)
        returnArray
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
