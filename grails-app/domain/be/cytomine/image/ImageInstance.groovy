package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.restapidoc.annotation.RestApiObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * An ImageInstance is an image map with a project
 */
@RestApiObject(name = "image instance",description="A link between 'abstract image' and 'project'. An 'abstract image' may be in multiple projects.")
class ImageInstance extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The image linked to the project")
    AbstractImage baseImage

    @RestApiObjectField(description = "The project that keeps the image")
    Project project

    @RestApiObjectField(description = "The user that add the image to the project")
    SecUser user

    @RestApiObjectField(description = "The number of user annotation in the image", useForCreation = false, apiFieldName = "numberOfAnnotations")
    Long countImageAnnotations = 0L

    @RestApiObjectField(description = "The number of job annotation in the image", useForCreation = false, apiFieldName = "numberOfJobAnnotations")
    Long countImageJobAnnotations = 0L

    @RestApiObjectField(description = "The number of reviewed annotation in the image", useForCreation = false, apiFieldName = "numberOfReviewedAnnotations")
    Long countImageReviewedAnnotations = 0L

    //stack stuff
    //TODO:APIDOC still used?
    Integer zIndex

    //TODO:APIDOC still used?
    Integer channel

    @RestApiObjectField(description = "The start review date", useForCreation = false)
    Date reviewStart

    @RestApiObjectField(description = "The stop review date", useForCreation = false)
    Date reviewStop

    @RestApiObjectField(description = "The user who reviewed (or still reviewing) this image", useForCreation = false)
    SecUser reviewUser

    @RestApiObjectField(description = "Instance image filename",useForCreation = false)
    String instanceFilename;

    @RestApiObjectFields(params=[
            @RestApiObjectField(apiFieldName = "filename", description = "Abstract image filename (see Abstract Image)",allowedType = "string",useForCreation = false),
            @RestApiObjectField(apiFieldName = "originalFilename", description = "Abstract image original filename (see Abstract Image)",allowedType = "string",useForCreation = false),
            @RestApiObjectField(apiFieldName = "path", description = "Abstract image path (see Abstract Image)",allowedType = "string",useForCreation = false),
            @RestApiObjectField(apiFieldName = "sample", description = "Abstract image sample (see Abstract Image)",allowedType = "long",useForCreation = false),
            @RestApiObjectField(apiFieldName = "mime", description = "Abstract image mime (see Abstract Image)",allowedType = "string",useForCreation = false),
            @RestApiObjectField(apiFieldName = "width", description = "Abstract image width (see Abstract Image)",allowedType = "int",useForCreation = false),
            @RestApiObjectField(apiFieldName = "height", description = "Abstract image height (see Abstract Image)",allowedType = "int",useForCreation = false),
            @RestApiObjectField(apiFieldName = "resolution", description = "Abstract image resolution (see Abstract Image)",allowedType = "double",useForCreation = false),
            @RestApiObjectField(apiFieldName = "magnification", description = "Abstract image magnification (see Abstract Image)",allowedType = "int",useForCreation = false),
            @RestApiObjectField(apiFieldName = "preview", description = "Abstract image preview (see Abstract Image)",allowedType = "string",useForCreation = false),
            @RestApiObjectField(apiFieldName = "thumb", description = "Abstract image thumb (see Abstract Image)",allowedType = "string",useForCreation = false),
            @RestApiObjectField(apiFieldName = "reviewed", description = "Image has been reviewed",allowedType = "boolean",useForCreation = false),
            @RestApiObjectField(apiFieldName = "inReview", description = "Image currently reviewed",allowedType = "boolean",useForCreation = false),
            @RestApiObjectField(apiFieldName = "depth", description = "?",allowedType = "long",useForCreation = false)
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
        instanceFilename nullable: true
    }

    static mapping = {
        id generator: "assigned"
        baseImage fetch: 'join'
        sort "id"
        tablePerHierarchy true
        cache true
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
        domain.deleted = JSONUtils.getJSONAttrDate(json, "deleted")
        domain.updated = JSONUtils.getJSONAttrDate(json, "updated")
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new User(), false)
        domain.baseImage = JSONUtils.getJSONAttrDomain(json, "baseImage", new AbstractImage(), false)
        domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), false)
        domain.reviewStart = JSONUtils.getJSONAttrDate(json, "reviewStart")
        domain.reviewStop = JSONUtils.getJSONAttrDate(json, "reviewStop")
        domain.reviewUser = JSONUtils.getJSONAttrDomain(json, "reviewUser", new User(), false)
        domain.instanceFilename = JSONUtils.getJSONAttrStr(json, "instanceFilename", false)
        //Check review constraint
        if ((domain.reviewUser == null && domain.reviewStart != null) || (domain.reviewUser != null && domain.reviewStart == null) || (domain.reviewStart == null && domain.reviewStop != null))
            throw new WrongArgumentException("Review data are not valid: user=${domain.reviewUser} start=${domain.reviewStart} stop=${domain.reviewStop}")

        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def image) {

        def returnArray = CytomineDomain.getDataFromDomain(image)
        returnArray['baseImage'] = image?.baseImage?.id
        returnArray['project'] = image?.project?.id
        returnArray['user'] = image?.user?.id
        returnArray['filename'] = image?.baseImage?.filename
        returnArray['extension'] = image?.baseImage?.mime?.extension
        returnArray['originalMimeType'] = image?.baseImage?.originalMimeType()
        returnArray['originalFilename'] = image?.baseImage?.originalFilename
        returnArray['instanceFilename'] = image?.getFileName()
        returnArray['sample'] = image?.baseImage?.sample?.id
        returnArray['path'] = image?.baseImage?.path
        returnArray['mime'] = image?.baseImage?.mime?.mimeType
        returnArray['width'] = image?.baseImage?.width
        returnArray['height'] = image?.baseImage?.height
        returnArray['resolution'] = image?.baseImage?.resolution
        returnArray['magnification'] = image?.baseImage?.magnification
        returnArray['depth'] = image?.baseImage?.getZoomLevels()?.max
        try {returnArray['preview'] = image.baseImage ? UrlApi.getThumbImage(image.baseImage?.id, 1024) : null} catch (Exception e) {returnArray['preview'] = 'NO preview:' + e.toString()}
        try {returnArray['thumb'] = image.baseImage ? UrlApi.getThumbImage(image.baseImage?.id, 512) : null} catch (Exception e) {returnArray['thumb'] = 'NO THUMB:' + e.toString()}
        try {returnArray['macroURL'] = image.baseImage ? UrlApi.getAssociatedImage(image.baseImage?.id, "macro", 512) : null} catch (Exception e) {returnArray['macro'] = 'NO THUMB:' + e.toString()}
        try {returnArray['fullPath'] = image.baseImage ? image.baseImage.getFullPath() : null} catch (Exception e) {returnArray['thumb'] = 'NO THUMB:' + e.toString()}
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

    public String getFileName() {
        if(instanceFilename != null && instanceFilename.trim()!= '') {
            return instanceFilename
        }
        return baseImage.originalFilename
    }
}