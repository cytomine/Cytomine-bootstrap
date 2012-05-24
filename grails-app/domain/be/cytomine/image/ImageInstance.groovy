package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.ontology.Annotation
import be.cytomine.project.Project
import be.cytomine.project.Slide
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * To change this template use File | Settings | File Templates.
 */
class ImageInstance extends CytomineDomain {

    AbstractImage baseImage
    Project project
    Slide slide
    SecUser user
    Long countImageAnnotations = 0L

    static belongsTo = [AbstractImage, Project, User]

    static constraints = {
        baseImage(unique: ['project'])
        slide(nullable: true)
        countImageAnnotations nullable: true
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
        def anntotations = Annotation.findAllByImage(this)
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

        String slideId = jsonImage.slide.toString()
        if (!slideId.equals("null")) {
            image.slide = Slide.get(Long.parseLong(slideId))
            if (image.slide == null) throw new WrongArgumentException("Slide was not found with id:" + slideId)
        }
        else image.slide = null

        try {image.countImageAnnotations = Long.parseLong(jsonImage.numberOfAnnotations.toString()) } catch (Exception e) {
            image.countImageAnnotations = 0
        }

        return image;
    }



    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + ImageInstance.class
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

            if (it.slideId) returnArray['slide'] = it.slideId
            else returnArray['slide'] = it.slide?.id

            returnArray['path'] = it.baseImage.path
            returnArray['mime'] = it.baseImage?.mime?.extension

            returnArray['width'] = it.baseImage.width
            returnArray['height'] = it.baseImage.height
            returnArray['resolution'] = it.baseImage.resolution
            returnArray['magnification'] = it.baseImage.magnification
            returnArray['depth'] = it.baseImage.getZoomLevels()?.max
            /*returnArray['scale'] = it.baseImage.scale

            returnArray['roi'] = it.baseImage.roi.toString()*/

            //returnArray['info'] = it.baseImage.slide?.name
            //returnArray['annotations'] = it.annotations
            // returnArray['thumb'] = it.baseImage.getThumbURL()
            //returnArray['preview'] = it.baseImage ? it.baseImage.getPreviewURL() : null
			try {returnArray['preview'] = it.baseImage ? it.baseImage.getPreviewURL() : null} catch (Exception e) {returnArray['preview'] = 'NO preview:' + e.toString()}
			try {returnArray['thumb'] = it.baseImage ? it.baseImage.getThumbURL() : null} catch (Exception e) {returnArray['thumb'] = 'NO THUMB:' + e.toString()}
            //returnArray['thumb'] = UrlApi.getThumbURLWithImageId(it.id)
            returnArray['metadataUrl'] = UrlApi.getMetadataURLWithImageId(cytomineBaseUrl,it.baseImage.id)

            try {returnArray['numberOfAnnotations'] = it.countImageAnnotations} catch (Exception e) {e.printStackTrace(); returnArray['numberOfAnnotations'] = -1}
            //returnArray['browse'] = ConfigurationHolder.config.grails.serverURL + "/image/browse/" + it.id

            //returnArray['imageServerBaseURL'] = it.baseImage.getMime().imageServers().collect { it.getZoomifyUrl() }
            //returnArray['imageServerBaseURL'] = UrlApi.getImageServerInfosWithImageId(it.id)

            return returnArray
        }
    }

     public Project projectDomain() {
        return project;
    }

}
