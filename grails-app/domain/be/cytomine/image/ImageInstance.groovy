package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * An ImageInstance is an image map with a project
 */
@ApiObject(name = "image instance")
class ImageInstance extends CytomineDomain implements Serializable {

    @ApiObjectFieldLight(description = "The image linked to the project")
    AbstractImage baseImage

    @ApiObjectFieldLight(description = "The project that keeps the image")
    Project project

    @ApiObjectFieldLight(description = "The user that add the image to the project")
    SecUser user

    @ApiObjectFieldLight(description = "The number of user annotation in the image", useForCreation = false, apiFieldName = "numberOfAnnotations")
    Long countImageAnnotations = 0L

    @ApiObjectFieldLight(description = "The number of job annotation in the image", useForCreation = false, apiFieldName = "numberOfJobAnnotations")
    Long countImageJobAnnotations = 0L

    @ApiObjectFieldLight(description = "The number of reviewed annotation in the image", useForCreation = false, apiFieldName = "numberOfReviewedAnnotations")
    Long countImageReviewedAnnotations = 0L

    //stack stuff
    //TODO:APIDOC still used?
    Integer zIndex

    //TODO:APIDOC still used?
    Integer channel

    @ApiObjectFieldLight(description = "The start review date", useForCreation = false)
    Date reviewStart

    @ApiObjectFieldLight(description = "The stop review date", useForCreation = false)
    Date reviewStop

    @ApiObjectFieldLight(description = "The user who reviewed (or still reviewing) this image", useForCreation = false)
    SecUser reviewUser

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "filename", description = "Abstract image filename (see Abstract Image)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "originalFilename", description = "Abstract image original filename (see Abstract Image)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "path", description = "Abstract image path (see Abstract Image)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "mime", description = "Abstract image mime (see Abstract Image)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "width", description = "Abstract image width (see Abstract Image)",allowedType = "int",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "height", description = "Abstract image height (see Abstract Image)",allowedType = "int",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "resolution", description = "Abstract image resolution (see Abstract Image)",allowedType = "double",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "magnification", description = "Abstract image magnification (see Abstract Image)",allowedType = "int",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "preview", description = "Abstract image preview (see Abstract Image)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "thumb", description = "Abstract image thumb (see Abstract Image)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "reviewed", description = "Image has been reviewed",allowedType = "boolean",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "inReview", description = "Image currently reviewed",allowedType = "boolean",useForCreation = false)
     ])
    static transients = []

    static belongsTo = [AbstractImage, Project, User]

    static constraints = {
        baseImage(unique: ['project'])
        countImageAnnotations nullable: true
        zIndex(nullable: true) //order in z-stack referenced by stack
        channel(nullable: true)  //e.g. fluo channel
        reviewStart nullable: true
        reviewStop nullable: true
        reviewUser nullable: true
    }

    static mapping = {
        id generator: "assigned"
        baseImage fetch: 'join'
        sort "id"
        tablePerHierarchy true
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        ImageInstance.withNewSession {
            ImageInstance imageAlreadyExist = ImageInstance.findByBaseImageAndProject(baseImage, project)
            if (imageAlreadyExist != null && (imageAlreadyExist.id != id)) {
                throw new AlreadyExistException("Image " + baseImage?.filename + " already map with project " + project.name)
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static ImageInstance insertDataIntoDomain(def json, def domain = new ImageInstance()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.created = JSONUtils.getJSONAttrDate(json, "created")
        domain.updated = JSONUtils.getJSONAttrDate(json, "updated")
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new User(), false)
        domain.baseImage = JSONUtils.getJSONAttrDomain(json, "baseImage", new AbstractImage(), false)
        domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), false)
        domain.reviewStart = JSONUtils.getJSONAttrDate(json, "reviewStart")
        domain.reviewStop = JSONUtils.getJSONAttrDate(json, "reviewStop")
        domain.reviewUser = JSONUtils.getJSONAttrDomain(json, "reviewUser", new User(), false)
        //Check review constraint
        if ((domain.reviewUser == null && domain.reviewStart != null) || (domain.reviewUser != null && domain.reviewStart == null) || (domain.reviewStart == null && domain.reviewStop != null))
            throw new WrongArgumentException("Review data are not valid: user=${domain.reviewUser} start=${domain.reviewStart} stop=${domain.reviewStop}")

        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + ImageInstance.class)
        JSON.registerObjectMarshaller(ImageInstance) { image ->
            return getDataFromDomain(image)
        }
    }
    
    static def getDataFromDomain(def image) {

        def returnArray = CytomineDomain.getDataFromDomain(image)
        println image
        returnArray['baseImage'] = image?.baseImage?.id
        returnArray['project'] = image?.project?.id
        returnArray['user'] = image?.user?.id
        returnArray['filename'] = image?.baseImage?.filename
        returnArray['originalFilename'] = image?.baseImage?.originalFilename
        returnArray['sample'] = image?.baseImage?.sample?.id
        returnArray['path'] = image?.baseImage?.path
        returnArray['mime'] = image?.baseImage?.mime?.extension
        returnArray['width'] = image?.baseImage?.width
        returnArray['height'] = image?.baseImage?.height
        returnArray['resolution'] = image?.baseImage?.resolution
        returnArray['magnification'] = image?.baseImage?.magnification
        returnArray['depth'] = image?.baseImage?.getZoomLevels()?.max
        try {returnArray['preview'] = image.baseImage ? image.baseImage.getPreviewURL() : null} catch (Exception e) {returnArray['preview'] = 'NO preview:' + e.toString()}
        try {returnArray['thumb'] = image.baseImage ? image.baseImage.getThumbURL() : null} catch (Exception e) {returnArray['thumb'] = 'NO THUMB:' + e.toString()}
        try {returnArray['numberOfAnnotations'] = image?.countImageAnnotations} catch (Exception e) {returnArray['numberOfAnnotations'] = -1}
        try {returnArray['numberOfJobAnnotations'] = image?.countImageJobAnnotations} catch (Exception e) {returnArray['numberOfJobAnnotations'] = -1}
        try {returnArray['numberOfReviewedAnnotations'] = image?.countImageReviewedAnnotations} catch (Exception e) {returnArray['numberOfReviewedAnnotations'] = -1}
        returnArray['reviewStart'] = image?.reviewStart ? image.reviewStart?.time?.toString() : null
        returnArray['reviewStop'] = image?.reviewStop ? image.reviewStop?.time?.toString() : null
        returnArray['reviewUser'] = image?.reviewUser?.id
        returnArray['reviewed'] = image?.isReviewed()
        returnArray['inReview'] = image?.isInReviewMode()
        return returnArray
    }    
    
    
    

    /**
     * Flag to control if image is beeing review, and not yet validated
     * @return True if image is review but not validate, otherwise false
     */
    public boolean isInReviewMode() {
        return (reviewStart != null && reviewUser != null)
    }

    /**
     * Flag to control if image is validated
     * @return True if review user has validate this image
     */
    public boolean isReviewed() {
        return (reviewStop != null)
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return project.container();
    }

    /**
     * Return domain user (annotation user, image user...)
     * By default, a domain has no user.
     * You need to override userDomainCreator() in domain class
     * @return Domain user
     */
    @Override
    public SecUser userDomainCreator() {
        return user
    }

}
