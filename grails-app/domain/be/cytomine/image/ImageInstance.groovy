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
        domain.countImageAnnotations = JSONUtils.getJSONAttrLong(json, "numberOfAnnotations", 0)
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
        JSON.registerObjectMarshaller(ImageInstance) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['baseImage'] = it.baseImage?.id
            returnArray['project'] = it.project?.id
            returnArray['user'] = it.user?.id
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
            returnArray['filename'] = it.baseImage.filename
            returnArray['originalFilename'] = it.baseImage.originalFilename
            returnArray['sample'] = it.baseImage.sample?.id
            returnArray['path'] = it.baseImage.path
            returnArray['mime'] = it.baseImage.mime?.extension
            returnArray['width'] = it.baseImage.width
            returnArray['height'] = it.baseImage.height
            returnArray['resolution'] = it.baseImage.resolution
            returnArray['magnification'] = it.baseImage.magnification
            returnArray['depth'] = it.baseImage.getZoomLevels()?.max
            try {returnArray['preview'] = it.baseImage ? it.baseImage.getPreviewURL() : null} catch (Exception e) {returnArray['preview'] = 'NO preview:' + e.toString()}
            try {returnArray['thumb'] = it.baseImage ? it.baseImage.getThumbURL() : null} catch (Exception e) {returnArray['thumb'] = 'NO THUMB:' + e.toString()}
            try {returnArray['numberOfAnnotations'] = it.countImageAnnotations} catch (Exception e) {returnArray['numberOfAnnotations'] = -1}
            try {returnArray['numberOfJobAnnotations'] = it.countImageJobAnnotations} catch (Exception e) {returnArray['numberOfJobAnnotations'] = -1}
            try {returnArray['numberOfReviewedAnnotations'] = it.countImageReviewedAnnotations} catch (Exception e) {returnArray['numberOfReviewedAnnotations'] = -1}
            returnArray['reviewStart'] = it.reviewStart ? it.reviewStart.time.toString() : null
            returnArray['reviewStop'] = it.reviewStop ? it.reviewStop.time.toString() : null
            returnArray['reviewUser'] = it.reviewUser?.id
            returnArray['reviewed'] = it.isReviewed()
            returnArray['inReview'] = it.isInReviewMode()

            return returnArray
        }
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
     * Get the project link with this domain type
     * @return Project of this domain
     */
    public Project projectDomain() {
        return project;
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
