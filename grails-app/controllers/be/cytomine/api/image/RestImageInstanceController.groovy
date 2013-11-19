package be.cytomine.api.image

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.TooLongRequestException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Property
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.utils.Description
import be.cytomine.utils.GeometryUtils
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import groovy.sql.Sql
import org.springframework.security.access.AccessDeniedException

import java.awt.*
import java.awt.image.BufferedImage

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 * TODO:: doc/test/coverage this controller!
 * TODOSTEVBEN: clean unused method + doc method
 */
class RestImageInstanceController extends RestController {

    def segmentationService
    def imageProcessingService
    def imageInstanceService
    def projectService
    def abstractImageService
    def userAnnotationService
    def algoAnnotationService
    def reviewedAnnotationService
    def secUserService
    def termService
    def annotationListingService
    def cytomineService
    def taskService
    def annotationIndexService

    final static int MAX_SIZE_WINDOW_REQUEST = 5000 * 5000 //5k by 5k pixels

    def show = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageInstance", params.id)
        }
    }

    def listByUser = {
         responseSuccess(imageInstanceService.list(cytomineService.currentUser))
    }

    def listLastOpenImage = {
        responseSuccess(imageInstanceService.listLastOpened(cytomineService.currentUser))
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'))
        if (project && !params.tree) {
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

    def next = {
        def image = imageInstanceService.read(params.long('id'))
        def next = ImageInstance.findAllByProjectAndCreatedLessThan(image.project,image.created,[sort:'created',order:'desc',max:1])
        if(next && !next.isEmpty()) {
            responseSuccess(next.first())
        } else {
            responseSuccess([:])
        }
    }

    def previous = {
        def image = imageInstanceService.read(params.long('id'))
        def previous = ImageInstance.findAllByProjectAndCreatedGreaterThan(image.project,image.created,[sort:'created',order:'asc',max:1])
        if(previous && !previous.isEmpty()) {
            responseSuccess(previous.first())
        } else {
            responseSuccess([:])
        }
    }


    def add = {
        try {
            responseResult(imageInstanceService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def update = {
        update(imageInstanceService, request.JSON)
    }

    def delete = {
        delete(imageInstanceService, JSON.parse("{id : $params.id}"),null)
    }

    def windowUrl = {
        ImageInstance image = ImageInstance.read(params.long('id'))
        AbstractImage abstractImage = image.getBaseImage()
        int x = Integer.parseInt(params.x)
        int y = abstractImage.getHeight() - Integer.parseInt(params.y)
        int w = Integer.parseInt(params.w)
        int h = Integer.parseInt(params.h)
        responseSuccess([url : abstractImage.getCropURL(x, y, w, h)])
    }

    def window = {
        //TODO:: document this method
        ImageInstance image = ImageInstance.read(params.long('id'))
        AbstractImage abstractImage = image.getBaseImage()
        int x = Integer.parseInt(params.x)
        int y = abstractImage.getHeight() - Integer.parseInt(params.y)
        int w = Integer.parseInt(params.w)
        int h = Integer.parseInt(params.h)

        int maxZoom = abstractImage.getZoomLevels().max
        int zoom = (params.zoom != null && params.zoom != "") ? Math.max(Math.min(Integer.parseInt(params.zoom), maxZoom), 0) : 0
        int resizeWidth = w / Math.pow(2, zoom)
        int resizeHeight = h / Math.pow(2, zoom)
        if (resizeWidth * resizeHeight > MAX_SIZE_WINDOW_REQUEST) {
            responseError(new TooLongRequestException("Request window size is too large : W * H > MAX_SIZE_WINDOW_REQUEST ($MAX_SIZE_WINDOW_REQUEST)"))
        }
        try {
            String url = abstractImage.getCropURL(x, y, w, h)
            BufferedImage bufferedImage = getImageFromURL(url)
            //Resize image here, scaling with IIP return "strange" results. Mail was send to the creator of the project
            if (resizeWidth != w || resizeHeight != h ) {
                BufferedImage resizedImage = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                g.drawImage(bufferedImage, 0, 0, resizeWidth, resizeHeight, null);
                g.dispose();
                bufferedImage = resizedImage
            }

            responseBufferedImage(bufferedImage)
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
    }

    def cropGeometry = {
        //TODO:: document this method
        String geometrySTR = params.geometry
        println params
        def geometry = new WKTReader().read(geometrySTR)
        def annotation = new UserAnnotation(location: geometry)
        annotation.image = ImageInstance.read(params.id)
        responseImage(abstractImageService.crop(annotation, null))
    }

    def mask = {
        println "mask"
        //TODO:: document this method
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
                ReviewedAnnotationListing ral = new ReviewedAnnotationListing(project: image.getProject().id, terms: termsIDS, users: userIDS, images:imageIDS, bbox:roiGeometry, columnToPrint:['basic','meta','wkt','term']  )
                def result = annotationListingService.listGeneric(ral)
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

    private BufferedImage getMaskImage(AnnotationDomain annotation, Term term, Integer zoom, Boolean withAlpha) {
        //TODO:: document this method

        BufferedImage crop = getImageFromURL(abstractImageService.crop(annotation, zoom))
        BufferedImage mask = new BufferedImage(crop.getWidth(),crop.getHeight(),BufferedImage.TYPE_INT_ARGB);
        AbstractImage abstractImage = annotation.getImage().getBaseImage()

        Geometry geometry = annotation.getLocation()

        def boundaries = annotation.getBoundaries()
        double x_ratio = crop.getWidth() / boundaries.width
        double y_ratio = crop.getHeight() / boundaries.height

        mask = segmentationService.colorizeWindow(abstractImage, mask, [geometry], boundaries.topLeftX, abstractImage.getHeight() - boundaries.topLeftY, x_ratio, y_ratio)

        if (withAlpha)
            return imageProcessingService.applyMaskToAlpha(crop, mask)
        else
            return mask
    }

    def alphamaskUserAnnotation = {
        try {
            def annotation = UserAnnotation.read(params.annotation)
            def cropURL = alphamask(annotation,params)
            if(cropURL!=null) responseBufferedImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    def alphamaskAlgoAnnotation = {
        try {
            def annotation = AlgoAnnotation.read(params.annotation)
            def cropURL = alphamask(annotation,params)
            responseBufferedImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    def alphamaskReviewedAnnotation = {
        try {
            def annotation = ReviewedAnnotation.read(params.annotation)
            def cropURL = alphamask(annotation,params)
            responseBufferedImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    private def alphamask(AnnotationDomain annotation, def params) {
        //TODO:: document this method
        if (!annotation) {
            responseNotFound("Annotation", params.annotation)
        }
        Term term = Term.read(params.term)
        if (!term) {
            responseNotFound("Term", params.term)
        }
        if (!annotation.termsId().contains(term.id)) {
            response([ error : "Term not associated with annotation", annotation : annotation.id, term : term.id])
        }
        Integer zoom = null
        if (params.zoom != null) zoom = Integer.parseInt(params.zoom)

        def zoomMinMax = annotation.getImage().getBaseImage().getZoomLevels()
        if ((params.zoom != null) && (zoom > zoomMinMax.max)) {
            zoom = zoomMinMax.max
        } else if ((params.zoom != null) && (zoom < zoomMinMax.min)) {
            zoom = zoomMinMax.min
        }
        try {
            return getMaskImage(annotation, term, zoom, true)
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
        return null;
    }

    def cropmask = {
        //TODO:: document this method
        UserAnnotation annotation = UserAnnotation.read(params.annotation)
        if (!annotation) {
            responseNotFound("Annotation", params.annotation)
        }
        Term term = Term.read(params.term)
        if (!term) {
            responseNotFound("Term", params.term)
        }
        if (!annotation.termsId().contains(term.id)) {
            response([ error : "Term not associated with userAnnotation", annotation : annotation.id, term : term.id])
        }
        Integer zoom = null
        if (params.zoom != null) zoom = Integer.parseInt(params.zoom)
        log.info "zoom====$zoom"
        if (annotation == null)
            responseNotFound("Crop", "Annotation", params.annotation)
        else if ((params.zoom != null) && (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max))
            responseNotFound("Crop", "Zoom", zoom)
        else {
            try {
                responseBufferedImage(getMaskImage(annotation, term, zoom, false))
            } catch (Exception e) {
                log.error("GetThumb:" + e);
            }
        }

    }



    /**
     * Check if an abstract image is already map with one or more projects
     * If true, send an array with item {imageinstanceId,layerId,layerName,projectId, projectName, admin}
     */
    def retrieveSameImageOtherProject = {
        try {
            ImageInstance image = imageInstanceService.read(params.long('id'))
            if(image) {
                SecurityACL.checkIsAdminContainer(image.project,cytomineService.currentUser)
                def layers =  imageInstanceService.getLayersFromAbstractImage(image.baseImage,image, projectService.list(cytomineService.currentUser).collect{it.id},secUserService.listUsers(image.project).collect{it.id})
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
    def copyAnnotationFromSameAbstractImage = {
        try {
            ImageInstance image = imageInstanceService.read(params.long('id'))
            SecurityACL.checkIsAdminContainer(image.project,cytomineService.currentUser)
            Task task = taskService.read(params.getLong("task"))
            log.info "task ${task} is find for id = ${params.getLong("task")}"
            def layers = !params.layers? "" : params.layers.split(",")
            if(image && layers) {
                responseSuccess(imageInstanceService.copyLayers(image,layers,secUserService.listUsers(image.project).collect{it.id},task))
            } else {
                responseNotFound("Abstract Image",params.id)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }




}
