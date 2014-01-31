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
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType
import sun.misc.BASE64Decoder

/**
 * Controller for abstract image
 * An abstract image can be add in n projects
 */
@Api(name = "abstract image services", description = "Methods for managing an image. See image instance service to manage an instance of image in a project.")
class RestImageController extends RestController {

    def imagePropertiesService
    def abstractImageService
    def cytomineService
    def projectService
    def segmentationService
    def imageSequenceService

    def currentDomainName() {
        return "abstract image" //needed because not RestAbstractImageController...
    }

    /**
     * List all abstract image available on cytomine
     */
    //TODO:APIDOC
    def list() {
        SecUser user = cytomineService.getCurrentUser()
        if(params.rows!=null) {
            responseSuccess(abstractImageService.list(user, params.page, params.rows, params.sidx, params.sord, params.filename, params.createdstart, params.createdstop))
        } else {
            responseSuccess(abstractImageService.list(user))
        }
    }

    /**
     * List all abstract images for a project
     */
    @ApiMethodLight(description="Get all image having an instance in a project", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id")
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
    @ApiMethodLight(description="Get an image")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image id")
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
    @ApiMethodLight(description="Add a new image in the software. See 'upload file service' to upload an image")
    def add() {
        add(abstractImageService, request.JSON)
    }

    /**
     * Update a new image
     * TODO:: how to manage security here?
     */
    @ApiMethodLight(description="Update an image in the software")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image sequence id")
    ])
    def update() {
        update(abstractImageService, request.JSON)
    }

    /**
     * Delete a new image
     * TODO:: how to manage security here?
     */
    @ApiMethodLight(description="Delete an image sequence)")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image sequence id")
    ])
    def delete() {
        delete(abstractImageService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Get metadata URL for an images
     * If extract, populate data from metadata table into image object
     */
    @ApiMethodLight(description="Get metadata URL for an images")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image id"),
        @ApiParam(name="extract", type="boolean", paramType = ApiParamType.QUERY,description = "(Optional) If true, populate data from metadata table into image object")
    ])
    @ApiResponseObject(objectIdentifier = "[metadata:x]")
    def metadata() {
        def idImage = params.long('id')
        def extract = params.boolean('extract')
        if (extract) {
            AbstractImage image = abstractImageService.read(idImage)
            imagePropertiesService.extractUseful(image)
            image.save(flush : true)
        }
        def responseData = [:]
        responseData.metadata = abstractImageService.metadata(idImage)
        response(responseData)
    }

    /**
     * Extract image properties from file
     */
    @ApiMethodLight(description="Get all image file properties for a specific image.", listing = true)
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image id")
    ])
    @ApiResponseObject(objectIdentifier = "image property")
    def imageProperties() {
        responseSuccess(abstractImageService.imageProperties(params.long('id')))
    }

    /**
     * Get an image property
     */
    @ApiMethodLight(description="Get a specific image file property", listing = true)
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image file property id")
    ])
    @ApiResponseObject(objectIdentifier ="image property")
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
    @ApiMethodLight(description="Get a small image (thumb) for a specific image")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image id")
    ])
    @ApiResponseObject(objectIdentifier = "image (bytes)")
    def thumb() {
        def url = abstractImageService.thumb(params.long('id'))
        log.info  "url=$url"
        responseImage(url)
    }

    /**
     * Get image preview URL
     */
    @ApiMethodLight(description="Get an image (preview) for a specific image")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image id")
    ])
    @ApiResponseObject(objectIdentifier ="image (bytes)")
    def preview() {
        responseImage(abstractImageService.preview(params.long('id')))
    }

    //TODO:APIDOC
    def camera () {
        println "camera"
        println params.imgdata
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


    /**
     * Get all image servers URL for an image
     */
    @ApiMethodLight(description="Get all image servers URL for an image")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image id"),
        @ApiParam(name="merge", type="boolean", paramType = ApiParamType.QUERY,description = "(Optional) If not null, return url representing the merge of multiple image. Value an be channel, zstack, slice or time."),
        @ApiParam(name="channels", type="list", paramType = ApiParamType.QUERY,description = "(Optional) If merge is not null, the list of the sequence index to merge."),
        @ApiParam(name="colors", type="list", paramType = ApiParamType.QUERY,description = "(Optional) If merge is not null, the list of the color for each sequence index (colors.size == channels.size)"),
    ])
    @ApiResponseObject(objectIdentifier = "URL list")
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
            println "ids=$ids"
            println "colors=$colors"
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



