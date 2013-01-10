package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
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
    ImageStack stack
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
        stack(nullable: true)
        zIndex(nullable: true) //order in z-stack referenced by stack
        channel(nullable : true)  //e.g. fluo channel
        reviewStart nullable:true
        reviewStop nullable:true
        reviewUser nullable:true
    }

    static mapping = {
        id generator: "assigned"
        baseImage fetch: 'join'
    }

    void checkAlreadyExist() {
        ImageInstance.withNewSession {
            ImageInstance imageAlreadyExist=ImageInstance.findByBaseImageAndProject(baseImage, project)
            if(imageAlreadyExist!=null && (imageAlreadyExist.id!=id))  {
                throw new AlreadyExistException("Image " + baseImage?.filename + " already map with project " + project.name)
            }
        }
    }

    static ImageInstance createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static ImageInstance createFromData(jsonImage) {
        def image = new ImageInstance()
        getFromData(image, jsonImage)
    }

    static ImageInstance getFromData(image, jsonImage) {
        image.created = getJSONAttrDate(jsonImage,"created")
        image.updated = getJSONAttrDate(jsonImage,"updated")
        image.user = getJSONAttrDomain(jsonImage,"user",new User(),false)
        image.baseImage = getJSONAttrDomain(jsonImage,"baseImage",new AbstractImage(),false)
        image.project = getJSONAttrDomain(jsonImage,"project",new Project(),false)
        image.countImageAnnotations = getJSONAttrLong(jsonImage,"numberOfAnnotations",0)
        image.reviewStart = getJSONAttrDate(jsonImage,"reviewStart")
        image.reviewStop = getJSONAttrDate(jsonImage,"reviewStop")
        image.reviewUser = getJSONAttrDomain(jsonImage,"reviewUser",new User(),false)
        //Check review constraint
        if ((image.reviewUser==null && image.reviewStart!=null) ||(image.reviewUser!=null && image.reviewStart==null) || (image.reviewStart==null && image.reviewStop!=null))
            throw new WrongArgumentException("Review data are not valid: user=${image.reviewUser} start=${image.reviewStart} stop=${image.reviewStop}")

        return image;
    }



    static void registerMarshaller(String cytomineBaseUrl) {
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
        return (reviewStart!=null && reviewUser!=null)
    }

    /**
     * Flag to control if image is validated
     * @return True if review user has validate this image
     */
    public boolean isReviewed() {
        return (reviewStop!=null)
    }

    /**
     * Get the project link with this domain type
     * @return Project of this domain
     */
    public Project projectDomain() {
        return project;
    }

}
