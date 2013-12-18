package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * An ImageInstance is an image map with a project
 */
class ImageInstance extends CytomineDomain implements Serializable {

    AbstractImage baseImage
    Project project
    SecUser user
    Long countImageAnnotations = 0L
    Long countImageJobAnnotations = 0L
    Long countImageReviewedAnnotations = 0L

    //stack stuff
    Integer zIndex
    Integer channel

    Date reviewStart
    Date reviewStop
    SecUser reviewUser

    static belongsTo = [AbstractImage, Project, User]

    static constraints = {
        baseImage(unique: ['project'])
        countImageAnnotations nullable: true

        //stack stuff
//        stack(nullable: true)
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

        def returnArray = [:]
        returnArray['class'] = image.class
        returnArray['id'] = image.id
        returnArray['baseImage'] = image.baseImage?.id
        returnArray['project'] = image.project?.id
        returnArray['user'] = image.user?.id
        returnArray['created'] = image.created?.time?.toString()
        returnArray['updated'] = image.updated?.time?.toString()
        returnArray['filename'] = image.baseImage.filename
        returnArray['originalFilename'] = image.baseImage.originalFilename
        returnArray['sample'] = image.baseImage.sample?.id
        returnArray['path'] = image.baseImage.path
        returnArray['mime'] = image.baseImage.mime?.extension
        returnArray['width'] = image.baseImage.width
        returnArray['height'] = image.baseImage.height
        returnArray['resolution'] = image.baseImage.resolution
        returnArray['magnification'] = image.baseImage.magnification
        returnArray['depth'] = image.baseImage.getZoomLevels()?.max
        try {returnArray['preview'] = image.baseImage ? image.baseImage.getPreviewURL() : null} catch (Exception e) {returnArray['preview'] = 'NO preview:' + e.toString()}
        try {returnArray['thumb'] = image.baseImage ? image.baseImage.getThumbURL() : null} catch (Exception e) {returnArray['thumb'] = 'NO THUMB:' + e.toString()}
        try {returnArray['numberOfAnnotations'] = image.countImageAnnotations} catch (Exception e) {returnArray['numberOfAnnotations'] = -1}
        try {returnArray['numberOfJobAnnotations'] = image.countImageJobAnnotations} catch (Exception e) {returnArray['numberOfJobAnnotations'] = -1}
        try {returnArray['numberOfReviewedAnnotations'] = image.countImageReviewedAnnotations} catch (Exception e) {returnArray['numberOfReviewedAnnotations'] = -1}
        returnArray['reviewStart'] = image.reviewStart ? image.reviewStart.time.toString() : null
        returnArray['reviewStop'] = image.reviewStop ? image.reviewStop.time.toString() : null
        returnArray['reviewUser'] = image.reviewUser?.id
        returnArray['reviewed'] = image.isReviewed()
        returnArray['inReview'] = image.isInReviewMode()

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
