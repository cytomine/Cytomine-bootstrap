package be.cytomine.image
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.SequenceDomain
import be.cytomine.ontology.Annotation
import be.cytomine.rest.UrlApi
import be.cytomine.project.Slide

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * To change this template use File | Settings | File Templates.
 */
class ImageInstance extends SequenceDomain {

    AbstractImage baseImage
    Project project
    Slide slide
    User user
    Long countImageAnnotations = 0L

    static belongsTo = [AbstractImage, Project, User]

    static constraints = {
        baseImage(unique:['project'])
        slide (nullable: true)
        countImageAnnotations nullable: true
    }

    static mapping = {
        id generator : "assigned"
        baseImage fetch:'join'
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

    static ImageInstance createFromData(jsonImage) {
        def image = new ImageInstance()
        getFromData(image,jsonImage)
    }

    static ImageInstance getFromData(image,jsonImage) {
        println "getFromData:"+ jsonImage

        image.created = (!jsonImage.created.toString().equals("null"))  ? new Date(Long.parseLong(jsonImage.created)) : null
        image.updated = (!jsonImage.updated.toString().equals("null"))  ? new Date(Long.parseLong(jsonImage.updated)) : null

        String userId = jsonImage.user.toString()
        println "userId="+userId
        if(!userId.equals("null")) {
            image.user = User.get(Long.parseLong(userId))
            if(image.user==null) throw new IllegalArgumentException("User was not found with id:"+userId)
        }
        else image.user = null

        String baseImageId = jsonImage.baseImage.toString()
        println "baseImageId="+baseImageId
        if(!baseImageId.equals("null")) {
            image.baseImage = AbstractImage.get(Long.parseLong(baseImageId))
            if(image.baseImage==null) throw new IllegalArgumentException("BaseImage was not found with id:"+baseImageId)
        }
        else image.baseImage = null


        String projectId = jsonImage.project.toString()
        println "projectId="+projectId
        if(!projectId.equals("null")) {
            image.project = Project.get(Long.parseLong(projectId))
            if(image.project==null) throw new IllegalArgumentException("Project was not found with id:"+projectId)
        }
        else image.project = null

        String slideId = jsonImage.slide.toString()
         println "slideId="+slideId
        if(!slideId.equals("null")) {
            image.slide = Slide.get(Long.parseLong(slideId))
            if(image.slide==null) throw new IllegalArgumentException("Slide was not found with id:"+slideId)
        }
        else image.slide = null

        try {image.countImageAnnotations = Long.parseLong(jsonImage.numberOfAnnotations.toString()) } catch(Exception e) {
           image.countImageAnnotations=0
        }

        return image;
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + ImageInstance.class
        JSON.registerObjectMarshaller(ImageInstance) {
            def returnArray = [:]
            returnArray['class'] = it.class

            returnArray['id'] = it.id
            returnArray['baseImage'] = it.baseImage? it.baseImage.id : null
            returnArray['project'] = it.project? it.project.id : null
            returnArray['user'] = it.user? it.user.id : null

            returnArray['created'] = it.created? it.created.time.toString() : null
            returnArray['updated'] = it.updated? it.updated.time.toString() : null

            try {returnArray['thumb'] = it.baseImage? it.baseImage.getThumbURL() : null}catch(Exception e){returnArray['thumb']='NO THUMB:'+e.toString()}
            returnArray['filename'] = it.baseImage? it.baseImage.filename : null

            returnArray['slide'] = it.slide? it.slide.id : null

            returnArray['path'] = it.baseImage.path
            returnArray['mime'] = it.baseImage?.mime?.extension

            returnArray['width'] = it.baseImage.width
            returnArray['height'] = it.baseImage.height

            returnArray['scale'] = it.baseImage.scale

            returnArray['roi'] = it.baseImage.roi.toString()

            returnArray['info'] = it.baseImage.slide?.name
            //returnArray['annotations'] = it.annotations
            // returnArray['thumb'] = it.baseImage.getThumbURL()
            returnArray['preview'] = it.baseImage.getPreviewURL()
            //returnArray['thumb'] = UrlApi.getThumbURLWithImageId(it.id)
            returnArray['metadataUrl'] = UrlApi.getMetadataURLWithImageId(it.baseImage.id)

            try {returnArray['numberOfAnnotations'] = it.countImageAnnotations}catch(Exception e){e.printStackTrace();returnArray['numberOfAnnotations']=-1}
            //returnArray['browse'] = ConfigurationHolder.config.grails.serverURL + "/image/browse/" + it.id

            returnArray['imageServerBaseURL'] = it.baseImage.getMime().imageServers().collect { it.getBaseUrl() }

            return returnArray
        }
    }

}
