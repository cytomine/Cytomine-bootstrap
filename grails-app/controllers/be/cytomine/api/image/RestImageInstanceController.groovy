package be.cytomine.api.image

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.TooLongRequestException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.command.Command
import be.cytomine.command.EditCommand
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Property
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.utils.Description
import be.cytomine.utils.GeometryUtils
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import groovy.sql.Sql
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 */
@Api(name = "image instance services", description = "Methods for managing an abstract image in a project")
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

    final static int MAX_SIZE_WINDOW_REQUEST = 5000 * 5000 //5k by 5k pixels

    @ApiMethodLight(description="Get an image instance")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image instance id")
    ])    
    def show() {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageInstance", params.id)
        }
    }

    @ApiMethodLight(description="Get all image instance available for the current user", listing = true)
    def listByUser() {
         responseSuccess(imageInstanceService.list(cytomineService.currentUser))
    }

    @ApiMethodLight(description="Get the last opened image for the current user", listing = true)
    def listLastOpenImage() {
        def offset = params.long('offset')
        def max =params.long('max')
        params.offset = 0
        params.max = 0
        responseSuccess(imageInstanceService.listLastOpened(cytomineService.currentUser,offset,max))
    }

    @ApiMethodLight(description="Get all image instance for a specific project", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="tree", type="boolean", paramType = ApiParamType.QUERY, description = "(optional) Get a tree (with parent image as node)"),
        @ApiParam(name="sortColumn", type="string", paramType = ApiParamType.QUERY, description = "(optional) Column sort (created by default)"),
        @ApiParam(name="sortDirection", type="string", paramType = ApiParamType.QUERY, description = "(optional) Sort direction (desc by default)"),
        @ApiParam(name="search", type="string", paramType = ApiParamType.QUERY, description = "(optional) Original filename sreach filter (all by default)")
    ])
    def listByProject() {
        Project project = projectService.read(params.long('id'))
        if (params.datatables) {
            def where = "project_id = ${project.id}"
            def fieldFormat = []
            println "dataTablesService=$dataTablesService"
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

    @ApiMethodLight(description="Get the next project image (first image created before)", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The current image instance id"),
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

    @ApiMethodLight(description="Get the previous project image (first image created after)", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The current image instance id"),
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

    @ApiMethodLight(description="Add a new image in a project")
    def add() {
        try {
            responseResult(imageInstanceService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @ApiMethodLight(description="Update an image instance")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image instance id")
    ])
    def update() {
        update(imageInstanceService, request.JSON)
    }

    @ApiMethodLight(description="Delete an image from a project)")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image instance id")
    ])
    def delete() {
        delete(imageInstanceService, JSON.parse("{id : $params.id}"),null)

////          def domain = imageInstanceService.retrieve(JSON.parse("{id : $params.id}"))
////        println "======================> " + getInfo(params.id)
////        SecurityACL.check(domain.container(),ADMINISTRATION)
////        SecurityACL.checkReadOnly(domain.container())
////        def jsonNewData = JSON.parse(domain.encodeAsJSON())
////        jsonNewData.deleted = new Date().time
////        println "jsonNewData.deleted="+jsonNewData.deleted
////        SecUser currentUser = cytomineService.getCurrentUser()
////        Command c = new EditCommand(user: currentUser)
////        def result = imageInstanceService.executeCommand(c,domain,jsonNewData)
////        println "result=$result"
////        Project project = new Project(name:"coucou", ontology:Ontology.list().first())
////        project.save(flush:true)
////        responseResult(result)
//        //domain.project = null
////        domain.deleted = new Date()
////        def names = domain.dirtyPropertyNames
////
////        println names
//////        for (name in names) {
//////            def originalValue = b.getPersistentValue(name)
//////        }
////
////        //
////        domain = imageInstanceService.saveAndReturnDomain(domain)
////
////
////        domain.reviewStart = new Date()
////        names = domain.dirtyPropertyNames
////
////        println names
////        imageInstanceService.saveDomain(domain)
////        println "======================> " + getInfo(params.id)
//
////
//        def domain = ImageInstance.get(params.long("id"))
//        println "domain " + domain.reviewStart + " " + domain.version
//        domain.reviewStart = new Date()
//        domain.save(flush:true, failOnError:true)
//
//        println "domain " + domain.reviewStart + " " + domain.version
//        domain.reviewStart = new Date()
//        domain.save(flush:true, failOnError:true)
//
//        println "domain " + domain.reviewStart + " " + domain.version
//
//
//
////        def domain = Project.read(16623)
////        println "domain " + domain.name + " " + domain.version
////        domain.name = "BOTANIQUE-LEAVES1"
////        domain.deleted = new Date()
////        domain.save(flush:true, failOnError:true)
////
////        println "domain " + domain.name + " " + domain.version
////        domain.name = "BOTANIQUE-LEAVES2"
////        domain.save(flush:true, failOnError:true)
////
////        println "domain " + domain.name + " " + domain.version
//        response([])
    }

    def dataSource
    /**
     * Get all image id from project
     */
    public def getInfo(String id) {

        //better perf with sql request
        String request = "SELECT a.id, a.version,a.deleted FROM image_instance a WHERE id = $id"
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0] + ", " + it[1] + ", " + it[2]
        }
        return data
    }


    //TODO:APIDOC
    def windowUrl() {
        ImageInstance image = ImageInstance.read(params.long('id'))
        AbstractImage abstractImage = image.getBaseImage()
        def boundaries = [:]
        boundaries.topLeftX = params.int("x")
        boundaries.topLeftY = abstractImage.getHeight() - params.int("y")
        boundaries.width = params.int("w")
        boundaries.height = params.int("h")
        responseSuccess([url : abstractImage.getCropURL(boundaries)])
    }

    //TODO:APIDOC
    def window() {
        //TODO:: document this method
        ImageInstance image = ImageInstance.read(params.long('id'))
        AbstractImage abstractImage = image.getBaseImage()

        def boundaries = [:]
        boundaries.topLeftX = params.int("x")
        boundaries.topLeftY = abstractImage.getHeight() - params.int("y")
        boundaries.width = params.int("w")
        boundaries.height = params.int("h")

        if (boundaries.width  * boundaries.height > MAX_SIZE_WINDOW_REQUEST) {
            responseError(new TooLongRequestException("Request window size is too large : W * H > MAX_SIZE_WINDOW_REQUEST ($MAX_SIZE_WINDOW_REQUEST)"))
        }
        try {
            String url = abstractImage.getCropURL(boundaries)
            BufferedImage bufferedImage = ImageIO.read(new URL(url))
            if (params.zoom) {
                int maxZoom = abstractImage.getZoomLevels().max
                int zoom = (params.zoom != null && params.zoom != "") ? Math.max(Math.min(params.int("zoom"), maxZoom), 0) : 0
                int resizeWidth = boundaries.width / Math.pow(2, zoom)
                int resizeHeight = boundaries.height / Math.pow(2, zoom)
                bufferedImage = imageProcessingService.scaleImage(bufferedImage, resizeWidth, resizeHeight)
            }
            responseBufferedImage(bufferedImage)
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
    }

    //TODO:APIDOC
    def cropGeometry() {
        //TODO:: document this method
        String geometrySTR = params.geometry
        println params
        def geometry = new WKTReader().read(geometrySTR)
        def annotation = new UserAnnotation(location: geometry)
        annotation.image = ImageInstance.read(params.long("id"))
        responseBufferedImage(imageProcessingService.crop(annotation, params))
    }

    //TODO:APIDOC
    def mask() {
        println "mask"
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

        java.util.List<Long> termsIDS = params.terms?.split(",")?.collect {
            Long.parseLong(it)
        }
        if (!termsIDS) { //don't filter by term, take everything
            termsIDS = termService.getAllTermId(image.getProject())
        }

        java.util.List<Long> userIDS = params.users?.split(",")?.collect {
            Long.parseLong(it)
        }
        if (!userIDS) { //don't filter by users, take everything
            userIDS = secUserService.listLayers(image.getProject()).collect { it.id}
        }

        java.util.List<Long> imageIDS = [image.id]


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
            e.printStackTrace()
        }
    }

    @ApiMethodLight(description="Copy image metadata (description, properties...) from an image to another one")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image that get the data"),
        @ApiParam(name="based", type="long", paramType = ApiParamType.QUERY, description = "The image source for the data")
    ])
    @ApiResponseObject(objectIdentifier = "empty")
    def copyMetadata() {
        try {
            ImageInstance based = imageInstanceService.read(params.long('based'))
            ImageInstance image = imageInstanceService.read(params.long('id'))
            if(image && based) {
                SecurityACL.checkIsAdminContainer(image.project,cytomineService.currentUser)

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
    @ApiMethodLight(description="Get, for an image instance, all the project having the same abstract image with the same layer (user)", listing = true)
    @ApiResponseObject(objectIdentifier =  "[project_sharing_same_image]")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image that get the data"),
        @ApiParam(name="project", type="long", paramType = ApiParamType.QUERY, description = "The image source for the data")
    ])
    def retrieveSameImageOtherProject() {
        try {
            ImageInstance image = imageInstanceService.read(params.long('id'))
            Project project = projectService.read(params.long('project'))
            if(image) {
                SecurityACL.checkIsAdminContainer(image.project,cytomineService.currentUser)
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
    @ApiMethodLight(description="Copy all annotation (and term, desc, property,...) from an image to another image", listing = true)
    @ApiResponseObject(objectIdentifier = "[copy_annotation_image]")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image that get the data"),
        @ApiParam(name="task", type="long", paramType = ApiParamType.QUERY, description = "(Optional) The id of task that will be update during the request processing"),
        @ApiParam(name="giveMe", type="boolean", paramType = ApiParamType.QUERY, description = "If true, copy all annotation on the current user layer. If false or not mentioned, copy all anotation on the same layer as the source image"),
        @ApiParam(name="layers", type="list (x1_y1,x2_y2,...)", paramType = ApiParamType.QUERY, description = "List of couple 'idimage_iduser'")
    ])
    def copyAnnotationFromSameAbstractImage() {
        try {
            ImageInstance image = imageInstanceService.read(params.long('id'))
            SecurityACL.checkIsAdminContainer(image.project,cytomineService.currentUser)
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
        redirect (uri : abstractImageService.downloadURI(imageInstance.baseImage.id))
    }

    def metadata() {
        Long id = params.long("id")
        ImageInstance imageInstance = imageInstanceService.read(id)
        def responseData = [:]
        responseData.metadata = abstractImageService.metadata(imageInstance.baseImage.id)
        response(responseData)
    }

    def associated() {
        Long id = params.long("id")
        ImageInstance imageInstance = imageInstanceService.read(id)
        def associated = abstractImageService.getAvailableAssociatedImages(imageInstance.baseImage.id)
        responseSuccess(associated)
    }

    def label() {
        Long id = params.long("id")
        ImageInstance imageInstance = imageInstanceService.read(id)
        def associatedImage = abstractImageService.getAssociatedImage(imageInstance.baseImage.id, params.label, params.maxWidth)
        responseBufferedImage(associatedImage)
    }

    def imageProperties() {
        Long id = params.long("id")
        ImageInstance imageInstance = imageInstanceService.read(id)
        responseSuccess(abstractImageService.imageProperties(imageInstance.baseImage.id))
    }



}
