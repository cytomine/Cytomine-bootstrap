package be.cytomine.api.image

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.processing.ProcessingServer
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.annotation.RestApi
import org.restapidoc.pojo.RestApiParamType
import sun.misc.BASE64Decoder

/**
 * Controller for abstract image
 * An abstract image can be add in n projects
 */
@RestApi(name = "abstract image services", description = "Methods for managing an image. See image instance service to manage an instance of image in a project.")
class RestAbstractImageController extends RestController {

    def imagePropertiesService
    def abstractImageService
    def cytomineService
    def projectService
    def segmentationService
    def imageSequenceService
    def dataTablesService

    /**
     * List all abstract image available on cytomine
     */
    //TODO:APIDOC

    @RestApiMethod(description="Get all image available for the current user", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="project", type="long", paramType = RestApiParamType.PATH, description = "(Optional) If set, check if image is in project or not"),
        @RestApiParam(name="sortColumn", type="string", paramType = RestApiParamType.QUERY, description = "(optional) Column sort (created by default)"),
        @RestApiParam(name="sortDirection", type="string", paramType = RestApiParamType.QUERY, description = "(optional) Sort direction (desc by default)"),
        @RestApiParam(name="search", type="string", paramType = RestApiParamType.QUERY, description = "(optional) Original filename search filter (all by default)")
    ])
    def list() {
        SecUser user = cytomineService.getCurrentUser()
        if (params.datatables) {
            Project project = projectService.read(params.long("project"))
            responseSuccess(dataTablesService.process(params, AbstractImage, null, [],project))
        }  else {
            responseSuccess(abstractImageService.list(user))
        }
    }

    /**
     * List all abstract images for a project
     */
    @RestApiMethod(description="Get all image having an instance in a project", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    def listByProject() {
        Project project = Project.read(params.id)
        if (project) {
            responseSuccess(abstractImageService.list(project))
        } else {
            responseNotFound("Image", "Project", params.id)
        }
    }

    /**
     * Get a single image
     */
    @RestApiMethod(description="Get an image")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image id")
    ])
    def show() {
        AbstractImage image = abstractImageService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("Image", params.id)
        }
    }

    /**
     * Add a new image
     * TODO:: how to manage security here?
     */
    @RestApiMethod(description="Add a new image in the software. See 'upload file service' to upload an image")
    def add() {
        add(abstractImageService, request.JSON)
    }

    /**
     * Update a new image
     * TODO:: how to manage security here?
     */
    @RestApiMethod(description="Update an image in the software")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image sequence id")
    ])
    def update() {
        update(abstractImageService, request.JSON)
    }

    /**
     * Delete a new image
     * TODO:: how to manage security here?
     */
    @RestApiMethod(description="Delete an image sequence)")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image sequence id")
    ])
    def delete() {
        delete(abstractImageService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Get metadata URL for an images
     * If extract, populate data from metadata table into image object
     */
    @RestApiMethod(description="Get metadata URL for an images")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image id"),
        @RestApiParam(name="extract", type="boolean", paramType = RestApiParamType.QUERY,description = "(Optional) If true, populate data from metadata table into image object")
    ])
    @RestApiResponseObject(objectIdentifier = "[metadata:x]")
    def metadata() {
        def idImage = params.long('id')
        def extract = params.boolean('extract')
        AbstractImage image = abstractImageService.read(idImage)
        /*if (extract) {
            imagePropertiesService.clear(image)
            imagePropertiesService.extractUseful(image)
            image.save(flush : true)
        }*/
        def responseData = [:]
        responseData.metadata = abstractImageService.metadata(image)
        response(responseData)
    }

    /**
     * Extract image properties from file
     */
    @RestApiMethod(description="Get all image file properties for a specific image.", listing = true)
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image id")
    ])
    @RestApiResponseObject(objectIdentifier = "image property")
    def imageProperties() {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        responseSuccess(abstractImageService.imageProperties(abstractImage))
    }

    /**
     * Get an image property
     */
    @RestApiMethod(description="Get a specific image file property", listing = true)
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image file property id")
    ])
    @RestApiResponseObject(objectIdentifier ="image property")
    def imageProperty() {
        def imageProperty = abstractImageService.imageProperty(params.long('imageproperty'))
        if (imageProperty) {
            responseSuccess(imageProperty)
        } else {
            responseNotFound("ImageProperty", params.imageproperty)
        }
    }

    /**
     * Get image thumb URL
     */
    @RestApiMethod(description="Get a small image (thumb) for a specific image")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image id")
    ])
    @RestApiResponseObject(objectIdentifier = "image (bytes)")
    def thumb() {
        response.setHeader("max-age", "86400")
        int maxSize = params.int('maxSize',  256)
        responseBufferedImage(abstractImageService.thumb(params.long('id'), maxSize))
    }

    @RestApiMethod(description="Get available associated images", listing = true)
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image id")
    ])
    @RestApiResponseObject(objectIdentifier ="associated image labels")
    def associated() {
        def associated = abstractImageService.getAvailableAssociatedImages(params.long("id"))
        responseSuccess(associated)
    }

    /**
     * Get associated image
     */
    @RestApiMethod(description="Get an associated image of a abstract image (e.g. label, macro, thumnail")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image id"),
    @RestApiParam(name="label", type="string", paramType = RestApiParamType.PATH,description = "The associated image label")
    ])
    @RestApiResponseObject(objectIdentifier = "image (bytes)")
    def label() {
        response.setHeader("Max-Age", "86400")
        AbstractImage abstractImage = abstractImageService.read(params.long("id"))
        def associatedImage = abstractImageService.getAssociatedImage(abstractImage, params.label, params.maxWidth)
        responseBufferedImage(associatedImage)
    }

    /**
     * Get image preview URL
     */
    @RestApiMethod(description="Get an image (preview) for a specific image")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image id")
    ])
    @RestApiResponseObject(objectIdentifier ="image (bytes)")
    def preview() {
        response.setHeader("max-age", "86400")
        int maxSize = params.int('maxSize',  1024)
        responseBufferedImage(abstractImageService.thumb(params.long('id'), maxSize))
    }

    //TODO:APIDOC
    def camera () {
        //:to do : save image in database ?
        //:+ send email
        String imageData = params.imgdata.replace(' ', '+')
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] imageByte = decoder.decodeBuffer(imageData);
        response.setContentType "application/octet-stream"
        response.setHeader "Content-disposition", "attachment; filename=capture.png"
        response.getOutputStream() << imageByte
        response.getOutputStream().flush()
    }

    def download() {
        redirect (uri : abstractImageService.downloadURI(abstractImageService.read(params.long("id"))))
    }


    /**
     * Get Image Tile
     * @param id
     * @param params
     */
    def tile() {
        String imageServerURL = grailsApplication.config.grails.imageServerURL
        String tileURL = abstractImageService.tile(params)
        redirect (url : "$imageServerURL/proxy?url=$tileURL")
    }

    /**
     * Get all image servers URL for an image
     */
    @RestApiMethod(description="Get all image servers URL for an image")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image id"),
        @RestApiParam(name="merge", type="boolean", paramType = RestApiParamType.QUERY,description = "(Optional) If not null, return url representing the merge of multiple image. Value an be channel, zstack, slice or time."),
        @RestApiParam(name="channels", type="list", paramType = RestApiParamType.QUERY,description = "(Optional) If merge is not null, the list of the sequence index to merge."),
        @RestApiParam(name="colors", type="list", paramType = RestApiParamType.QUERY,description = "(Optional) If merge is not null, the list of the color for each sequence index (colors.size == channels.size)"),
    ])
    @RestApiResponseObject(objectIdentifier = "URL list")
    def imageServers() {

        try {
        def id = params.long('id')
        def merge = params.get('merge')

        if(merge) {
            def idImageInstance = params.long('imageinstance')
            ImageInstance image = ImageInstance.read(idImageInstance)

            log.info "Ai=$id Ii=$idImageInstance"

            ImageSequence sequence = imageSequenceService.get(image)

            if(!sequence) {
                throw new WrongArgumentException("ImageInstance $idImageInstance is not in a sequence!")
            }

            ImageGroup group = sequence.imageGroup

            log.info "sequence=$sequence group=$group"

            def images = imageSequenceService.list(group)

            log.info "all image for this group=$images"


            def servers = ProcessingServer.list()
            Random myRandomizer = new Random();


            def ids = params.get('channels').split(",").collect{Integer.parseInt(it)}
            def colors = params.get('colors').split(",").collect{it}
            def params = []

            ids.eachWithIndex {pos,index ->
                images.each { seq ->
                    def position = -1
                    if(merge=="channel") position = seq.channel
                    if(merge=="zstack") position = seq.zStack
                    if(merge=="slice") position = seq.slice
                    if(merge=="time") position = seq.time

                    if(position==pos) {
                        def urls = abstractImageService.imageServers(seq.image.baseImage.id).imageServersURLs
                        if(ids.contains(position)) {
                            def param = "url$index="+ URLEncoder.encode(urls.first(),"UTF-8") +"&color$index="+ URLEncoder.encode(colors.get(index),"UTF-8")
                            params << param
                        }
                    }

                }

            }


            String url = "vision/merge?" + params.join("&") +"&zoomify="
            log.info "url=$url"

            def urls = []

            (0..5).each {
                urls << servers.get(myRandomizer.nextInt(servers.size())).url + url
            }

            //retrieve all image instance (same sequence)


            //get url for each image

            responseSuccess([imageServersURLs : urls])
        } else {
            responseSuccess(abstractImageService.imageServers(id))
        }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

}



