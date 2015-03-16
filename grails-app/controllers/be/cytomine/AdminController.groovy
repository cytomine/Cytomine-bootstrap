package be.cytomine

import be.cytomine.api.RestController
import grails.plugin.springsecurity.annotation.Secured

import javax.imageio.ImageIO

@Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
class AdminController extends RestController {

    def imageRetrievalService

    def grailsApplication
    def modelService
    def springSecurityService
    def archiveCommandService

    @Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
    def index() {
      //don't remove this, it calls admin/index.gsp layout !
    }

    @Secured(['ROLE_ADMIN','ROLE_SUPER_ADMIN'])
    def archive() {
        archiveCommandService.archiveOldCommand()
        responseSuccess([])




    }

    @Secured(['ROLE_USER','ROLE_ADMIN','ROLE_SUPER_ADMIN'])
    def testRetrieval() {
        def response = imageRetrievalService.indexImage(
                ImageIO.read(new File("/home/lrollus/git/CBIRestAPI/testdata/images/crop5.jpg")),
                new Date().getTime()+"",
                "toto",
                new HashMap<>()
        )
        println response
        render response
    }



}
