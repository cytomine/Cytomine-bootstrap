package be.cytomine

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.ontology.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.utils.GisUtils
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.MultiPolygon
import com.vividsolutions.jts.io.WKTReader
import geb.Browser
import grails.plugins.springsecurity.Secured
import groovy.sql.Sql

@Secured(['ROLE_ADMIN'])
class AdminController extends RestController {


    def grailsApplication
    def modelService
    def springSecurityService
    def archiveCommandService
    def simplifyGeometryService

    def index() {
      //don't remove this, it calls admin/index.gsp layout !
    }



    def archive() {
        archiveCommandService.archiveOldCommand()
        responseSuccess([])
    }
}
