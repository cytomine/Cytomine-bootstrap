package be.cytomine.api.image

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.TooLongRequestException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.*
import be.cytomine.project.Project
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.utils.Description
import be.cytomine.utils.GeometryUtils
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON

import java.awt.*
import java.awt.image.BufferedImage

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 */
class RestNestedImageInstanceController extends RestController {

    def segmentationService
    def imageProcessingService
    def nestedImageInstanceService
    def imageInstanceService
    def projectService
    def abstractImageService
    def userAnnotationService
    def algoAnnotationService
    def reviewedAnnotationService
    def secUserService
    def termService
    def cytomineService
    def taskService


    def show = {
        ImageInstance image = nestedImageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("NestedImageInstance", params.id)
        }
    }


    def listByImageInstance = {
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        if (image)  {
            responseSuccess(nestedImageInstanceService.list(image))
        }
        else {
            responseNotFound("NestedImageInstance", "Image", params.idImage)
        }
    }

    def add = {
        try {
            responseResult(nestedImageInstanceService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def update = {
        update(nestedImageInstanceService, request.JSON)
    }

    def delete = {
        delete(nestedImageInstanceService, JSON.parse("{id : $params.id}"),null)
    }
}
