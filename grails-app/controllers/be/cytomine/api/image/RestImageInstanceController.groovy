package be.cytomine.api.image

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.TooLongRequestException

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.UploadedFile
import be.cytomine.ontology.Property
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.utils.Description
import be.cytomine.utils.GeometryUtils
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import groovy.sql.Sql
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.pojo.RestApiParamType

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 */
@RestApi(name = "image instance services", description = "Methods for managing an abstract image in a project")
class RestImageInstanceController extends RestController {

    def segmentationService
    def imageProcessingService
    def imageInstanceService
    def projectService
    def abstractImageService
    def dataTablesService
    def userAnnotationService
    def algoAnnotationService
    def reviewedAnnotationService
    def secUserService
    def termService
    def annotationListingService
    def cytomineService
    def taskService
    def annotationIndexService
    def descriptionService
    def propertyService
    def securityACLService

    final static int MAX_SIZE_WINDOW_REQUEST = 5000 * 5000 //5k by 5k pixels

    @RestApiMethod(description="Get an image instance")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image instance id")
    ])
    def show() {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageInstance", params.id)
        }
    }

    @RestApiMethod(description="Get all image instance available for the current user", listing = true)
    def listByUser() {
        responseSuccess(imageInstanceService.list(cytomineService.currentUser))
    }

    @RestApiMethod(description="Get the last opened image for the current user", listing = true)
    def listLastOpenImage() {
        def offset = params.long('offset')
        def max =params.long('max')
        params.offset = 0
        params.max = 0
        responseSuccess(imageInstanceService.listLastOpened(cytomineService.currentUser,offset,max))
    }

    @RestApiMethod(description="Get all image instance for a specific project", listing = true)
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id"),
    @RestApiParam(name="tree", type="boolean", paramType = RestApiParamType.QUERY, description = "(optional) Get a tree (with parent image as node)"),
    @RestApiParam(name="sortColumn", type="string", paramType = RestApiParamType.QUERY, description = "(optional) Column sort (created by default)"),
    @RestApiParam(name="sortDirection", type="string", paramType = RestApiParamType.QUERY, description = "(optional) Sort direction (desc by default)"),
    @RestApiParam(name="search", type="string", paramType = RestApiParamType.QUERY, description = "(optional) Original filename sreach filter (all by default)")
    ])
    def listByProject() {
        Project project = projectService.read(params.long('id'))
        if (params.datatables) {
            def where = "project_id = ${project.id}"
            def fieldFormat = []
            responseSuccess(dataTablesService.process(params, ImageInstance, where, fieldFormat,project))
        }
        else if (project && !params.tree) {
            String sortColumn = params.sortColumn ? params.sortColumn : "created"
            String sortDirection = params.sortDirection ? params.sortDirection : "desc"
            String search = params.search
            responseSuccess(imageInstanceService.list(project, sortColumn, sortDirection, search))
        }
        else if (project && params.tree && params.boolean("tree"))  {
            responseSuccess(imageInstanceService.listTree(project))
        }
        else {
            responseNotFound("ImageInstance", "Project", params.id)
        }
    }

    @RestApiMethod(description="Get the next project image (first image created before)", listing = true)
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The current image instance id"),
    ])
    def next() {
        def image = imageInstanceService.read(params.long('id'))
        def next = ImageInstance.findAllByProjectAndCreatedLessThan(image.project,image.created,[sort:'created',order:'desc',max:1])
        if(next && !next.isEmpty()) {
            responseSuccess(next.first())
        } else {
            responseSuccess([:])
        }
    }

    @RestApiMethod(description="Get the previous project image (first image created after)", listing = true)
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The current image instance id"),
    ])
    def previous() {
        def image = imageInstanceService.read(params.long('id'))
        def previous = ImageInstance.findAllByProjectAndCreatedGreaterThan(image.project,image.created,[sort:'created',order:'asc',max:1])
        if(previous && !previous.isEmpty()) {
            responseSuccess(previous.first())
        } else {
            responseSuccess([:])
        }
    }

    @RestApiMethod(description="Add a new image in a project")
    def add() {
        try {
            responseResult(imageInstanceService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @RestApiMethod(description="Update an image instance")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image instance id")
    ])
    def update() {
        update(imageInstanceService, request.JSON)
    }

    @RestApiMethod(description="Delete an image from a project)")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image instance id")
    ])
    def delete() {
        delete(imageInstanceService, JSON.parse("{id : $params.id}"),null)
    }

    def dataSource
    /**
     * Get all image id from project
     */
    public def getInfo(String id) {

        //better perf with sql request
        String request = "SELECT a.id, a.version,a.deleted FROM image_instance a WHERE id = $id"
        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            data << it[0] + ", " + it[1] + ", " + it[2]
        }
        try {
            sql.close()
        }catch (Exception e) {}
        return data
    }


    //TODO:APIDOC
    def windowUrl() {
        ImageInstance imageInstance = imageInstanceService.read(params.id)
        params.id = imageInstance.baseImage.id
        responseSuccess([url : abstractImageService.window(params, request.queryString)])
    }

    //TODO:APIDOC
    def window() {
        ImageInstance imageInstance = imageInstanceService.read(params.id)
        params.id = imageInstance.baseImage.id
        redirect(url : abstractImageService.window(params, request.queryString))
    }

    //TODO:APIDOC
    def cropGeometry() {
        //TODO:: document this method
        String geometrySTR = params.geometry
        def geometry = new WKTReader().read(geometrySTR)
        def annotation = new UserAnnotation(location: geometry)
        annotation.image = ImageInstance.read(params.long("id"))
        redirect (url : annotation.toCropURL(params))
    }

    //TODO:APIDOC
    def mask() {
        //TODO:: document this method
        //TODO:: make alphamask
        ImageInstance image = ImageInstance.read(params.long('id'))
        AbstractImage abstractImage = image.getBaseImage()

        int x = params.int("x")
        int y = params.int("y")
        int w = params.int("w")
        int h = params.int("h")

        boolean review = params.boolean("review")

        if (w * h > MAX_SIZE_WINDOW_REQUEST) {
            responseError(new TooLongRequestException("Request window size is too large : W * H > MAX_SIZE_WINDOW_REQUEST ($MAX_SIZE_WINDOW_REQUEST)"))
        }

        List<Long> termsIDS = params.terms?.split(',')?.collect {
            Long.parseLong(it)
        }
        if (!termsIDS) { //don't filter by term, take everything
            termsIDS = termService.getAllTermId(image.getProject())
        }

        List<Long> userIDS = params.users?.split(",")?.collect {
            Long.parseLong(it)
        }
        if (!userIDS) { //don't filter by users, take everything
            userIDS = secUserService.listLayers(image.getProject()).collect { it.id}
        }

        List<Long> imageIDS = [image.id]


        try {
            //Create a geometry corresponding to the ROI of the request (x,y,w,h)
            Geometry roiGeometry = GeometryUtils.createBoundingBox(
                    x,                                      //minX
                    x + w,                                  //maxX
                    abstractImage.getHeight() - (y + h),    //minX
                    abstractImage.getHeight() - y           //maxY
            )

            //Get the image, compute ratio between asked and received
            BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

            //Fetch annotations with the requested term on the request image

            ArrayList<Geometry> geometries
            if (review) {
                ReviewedAnnotationListing ral = new ReviewedAnnotationListing(project: image.getProject().id, terms: termsIDS, reviewUsers: userIDS, images:imageIDS, bbox:roiGeometry, columnToPrint:['basic','meta','wkt','term']  )
                def result = annotationListingService.listGeneric(ral)
                log.info "annotations=${result.size()}"
                geometries = result.collect {
                    new WKTReader().read(it["location"])
                }

            } else {
                Collection<UserAnnotation> annotations = userAnnotationService.list(image, roiGeometry, termsIDS, userIDS)
                geometries = annotations.collect { geometry ->
                    geometry.getLocation()
                }
            }

            //Draw annotation
            mask = segmentationService.colorizeWindow(abstractImage, mask, geometries, x, y, 1, 1)

            responseBufferedImage(mask)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    @RestApiMethod(description="Copy image metadata (description, properties...) from an image to another one")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image that get the data"),
    @RestApiParam(name="based", type="long", paramType = RestApiParamType.QUERY, description = "The image source for the data")
    ])
    @RestApiResponseObject(objectIdentifier = "empty")
    def copyMetadata() {
        try {
            ImageInstance based = imageInstanceService.read(params.long('based'))
            ImageInstance image = imageInstanceService.read(params.long('id'))
            if(image && based) {
                securityACLService.checkIsAdminContainer(image.project,cytomineService.currentUser)

                Description.findAllByDomainIdent(based.id).each { description ->
                    def json = JSON.parse(description.encodeAsJSON())
                    json.domainIdent = image.id
                    descriptionService.add(json)
                }

                Property.findAllByDomainIdent(based.id).each { property ->
                    def json = JSON.parse(property.encodeAsJSON())
                    json.domainIdent = image.id
                    propertyService.add(json)
                }

                responseSuccess([])
            } else if(!based) {
                responseNotFound("ImageInstance",params.based)
            }else if(!image) {
                responseNotFound("ImageInstance",params.id)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }

    }

    /**
     * Check if an abstract image is already map with one or more projects
     * If true, send an array with item {imageinstanceId,layerId,layerName,projectId, projectName, admin}
     */
    @RestApiMethod(description="Get, for an image instance, all the project having the same abstract image with the same layer (user)", listing = true)
    @RestApiResponseObject(objectIdentifier =  "[project_sharing_same_image]")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image that get the data"),
    @RestApiParam(name="project", type="long", paramType = RestApiParamType.QUERY, description = "The image source for the data")
    ])
    def retrieveSameImageOtherProject() {
        try {
            ImageInstance image = imageInstanceService.read(params.long('id'))
            Project project = projectService.read(params.long('project'))
            if(image) {
                securityACLService.checkIsAdminContainer(image.project,cytomineService.currentUser)
                def layers =  imageInstanceService.getLayersFromAbstractImage(image.baseImage,image, projectService.list(cytomineService.currentUser).collect{it.id},secUserService.listUsers(image.project).collect{it.id},project)
                responseSuccess(layers)
            } else {
                responseNotFound("Abstract Image",params.id)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    /**
     * Copy all annotation (and dedepency: term, description, property,..) to the new image
     * Params must be &layers=IMAGEINSTANCE1_USER1,IMAGE_INSTANCE1_USER2,... which will add annotation
     * from user/image from another project.
     */
    @RestApiMethod(description="Copy all annotation (and term, desc, property,...) from an image to another image", listing = true)
    @RestApiResponseObject(objectIdentifier = "[copy_annotation_image]")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image that get the data"),
    @RestApiParam(name="task", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) The id of task that will be update during the request processing"),
    @RestApiParam(name="giveMe", type="boolean", paramType = RestApiParamType.QUERY, description = "If true, copy all annotation on the current user layer. If false or not mentioned, copy all anotation on the same layer as the source image"),
    @RestApiParam(name="layers", type="list (x1_y1,x2_y2,...)", paramType = RestApiParamType.QUERY, description = "List of couple 'idimage_iduser'")
    ])
    def copyAnnotationFromSameAbstractImage() {
        try {
            ImageInstance image = imageInstanceService.read(params.long('id'))
            securityACLService.checkIsAdminContainer(image.project,cytomineService.currentUser)
            Task task = taskService.read(params.getLong("task"))
            Boolean giveMe = params.boolean("giveMe")
            log.info "task ${task} is find for id = ${params.getLong("task")}"
            def layers = !params.layers? "" : params.layers.split(",")
            if(image && layers) {
                responseSuccess(imageInstanceService.copyLayers(image,layers,secUserService.listUsers(image.project).collect{it.id},task,cytomineService.currentUser,giveMe))
            } else {
                responseNotFound("Abstract Image",params.id)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def download() {
        Long id = params.long("id")
        ImageInstance imageInstance = imageInstanceService.read(id)
        redirect (uri : abstractImageService.downloadURI(imageInstance.baseImage))
    }

    /*def metadata() {
        Long id = params.long("id")
        ImageInstance imageInstance = imageInstanceService.read(id)
        def responseData = [:]
        responseData.metadata = abstractImageService.metadata(imageInstance.baseImage)
        response(responseData)
    }*/

    def associated() {
        Long id = params.long("id")
        ImageInstance imageInstance = imageInstanceService.read(id)
        def associated = abstractImageService.getAvailableAssociatedImages(imageInstance.baseImage)
        responseSuccess(associated)
    }

    def label() {
        Long id = params.long("id")
        String label = params.label
        def maxWidth = 1000
        if (params.maxWidth) {
            maxWidth = params.int("maxWidth")
        }
        ImageInstance imageInstance = imageInstanceService.read(id)
        def associatedImage = abstractImageService.getAssociatedImage(imageInstance.baseImage, label, maxWidth)
        if (associatedImage)
            responseBufferedImage(associatedImage)
    }

    def imageProperties() {
        Long id = params.long("id")
        ImageInstance imageInstance = imageInstanceService.read(id)
        responseSuccess(abstractImageService.imageProperties(imageInstance.baseImage))
    }



}
