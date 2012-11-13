package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project

import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.ontology.UserAnnotation

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * To change this template use File | Settings | File Templates.
 */
class ImageInstance extends CytomineDomain implements Serializable {

    AbstractImage baseImage
    Project project
    SecUser user
    Long countImageAnnotations = 0L
    Long countImageJobAnnotations = 0L

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
            if(imageAlreadyExist!=null && (imageAlreadyExist.id!=id))  throw new AlreadyExistException("Image " + baseImage?.filename + " already map with project " + project.name)
        }
    }

    def terms() {
        def terms = []
        def anntotations = UserAnnotation.findAllByImage(this)
        anntotations.each { annotation ->
            annotation.terms().each { term ->
                terms << term
            }
        }
        terms
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
        image.created = (!jsonImage.created.toString().equals("null")) ? new Date(Long.parseLong(jsonImage.created)) : null
        image.updated = (!jsonImage.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonImage.updated)) : null

        String userId = jsonImage.user.toString()
        if (!userId.equals("null")) {
            image.user = User.get(Long.parseLong(userId))
            if (image.user == null) throw new WrongArgumentException("User was not found with id:" + userId)
        }
        else image.user = null

        String baseImageId = jsonImage.baseImage.toString()
        if (!baseImageId.equals("null")) {
            image.baseImage = AbstractImage.get(Long.parseLong(baseImageId))
            if (image.baseImage == null) throw new WrongArgumentException("BaseImage was not found with id:" + baseImageId)
        }
        else image.baseImage = null

        String projectId = jsonImage.project.toString()
        if (!projectId.equals("null")) {
            image.project = Project.get(Long.parseLong(projectId))
            if (image.project == null) throw new WrongArgumentException("Project was not found with id:" + projectId)
        }
        else image.project = null

        try {image.countImageAnnotations = Long.parseLong(jsonImage.numberOfAnnotations.toString()) } catch (Exception e) {
            image.countImageAnnotations = 0
        }

        image.reviewStart = (!jsonImage.reviewStart.toString().equals("null")) ? new Date(Long.parseLong(jsonImage.reviewStart)) : null
        image.reviewStop = (!jsonImage.reviewStop.toString().equals("null")) ? new Date(Long.parseLong(jsonImage.reviewStop)) : null

        String reviewUserId = jsonImage.reviewUser.toString()
        if (!reviewUserId.equals("null")) {
            image.reviewUser = User.get(Long.parseLong(reviewUserId))
            if (image.reviewUser == null) throw new WrongArgumentException("User was not found with id:" + reviewUserId)
        }
        else image.reviewUser = null

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


            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null

            
            returnArray['filename'] = it.baseImage ? it.baseImage.filename : null
            returnArray['originalFilename'] = it.baseImage ? it.baseImage.originalFilename : null

            returnArray['sample'] = it.baseImage ? it.baseImage.sample : null

            returnArray['path'] = it.baseImage.path
            returnArray['mime'] = it.baseImage?.mime?.extension

            returnArray['width'] = it.baseImage.width
            returnArray['height'] = it.baseImage.height
            returnArray['resolution'] = it.baseImage.resolution
            returnArray['magnification'] = it.baseImage.magnification
            returnArray['depth'] = it.baseImage.getZoomLevels()?.max
            /*returnArray['scale'] = it.baseImage.scale

            returnArray['roi'] = it.baseImage.roi.toString()*/

            //returnArray['info'] = it.baseImage.sample?.name
            //returnArray['annotations'] = it.annotations
            // returnArray['thumb'] = it.baseImage.getThumbURL()
            //returnArray['preview'] = it.baseImage ? it.baseImage.getPreviewURL() : null
			try {returnArray['preview'] = it.baseImage ? it.baseImage.getPreviewURL() : null} catch (Exception e) {returnArray['preview'] = 'NO preview:' + e.toString()}

            //returnArray['thumb'] = UrlApi.getPreviewURLWithImageId(cytomineBaseUrl, it.baseImage.id)
            try {returnArray['thumb'] = it.baseImage ? it.baseImage.getThumbURL() : null} catch (Exception e) {returnArray['thumb'] = 'NO THUMB:' + e.toString()}

            //returnArray['metadataUrl'] = UrlApi.getMetadataURLWithImageId(cytomineBaseUrl,it.baseImage.id)

            try {returnArray['numberOfAnnotations'] = it.countImageAnnotations} catch (Exception e) {returnArray['numberOfAnnotations'] = -1}
            try {returnArray['numberOfJobAnnotations'] = it.countImageJobAnnotations} catch (Exception e) {returnArray['numberOfJobAnnotations'] = -1}
            //returnArray['browse'] = ConfigurationHolder.config.grails.serverURL + "/image/browse/" + it.id

            //returnArray['imageServerBaseURL'] = it.baseImage.getMime().imageServers().collect { it.getZoomifyUrl() }
            //returnArray['imageServerBaseURL'] = UrlApi.getImageServerInfosWithImageId(it.id)

            returnArray['reviewStart'] = it.reviewStart ? it.reviewStart.time.toString() : null
            returnArray['reviewStop'] = it.reviewStop ? it.reviewStop.time.toString() : null
            returnArray['reviewUser'] = it.reviewUser

            returnArray['reviewed'] = it.isReviewed()
            returnArray['inReview'] = it.isInReviewMode()

            return returnArray
        }
    }

    public boolean isInReviewMode() {
        return (reviewStart!=null && reviewUser!=null)
    }

    public boolean isReviewed() {
        return (reviewStop!=null)
    }

     public Project projectDomain() {
        return project;
    }

}
