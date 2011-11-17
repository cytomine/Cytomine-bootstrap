package be.cytomine.api

import be.cytomine.image.AbstractImage
import grails.converters.*
import be.cytomine.ontology.Annotation
import be.cytomine.image.server.RetrievalServer
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.abstractimage.AddAbstractImageCommand

import be.cytomine.project.Project
import be.cytomine.command.abstractimage.EditAbstractImageCommand
import be.cytomine.command.abstractimage.DeleteAbstractImageCommand
import be.cytomine.api.RestController

import grails.orm.PagedResultList
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServer

class RestImageController extends RestController{

    def springSecurityService
    def transactionService
    def imagePropertiesService
    def storageService

    def index = {
        redirect(controller: "image")
    }
    def list = {
        response(AbstractImage.list())
    }

      def listByUser = {
        def data = [:]

        User user=null
        if(params.id!=null) {
          user = User.read(params.id)
        } else {
           user = getCurrentUser(springSecurityService.principal.id)
        }

        String page = params.page
        String limit = params.rows
        String sortedRow = params.sidx
        String sord = params.sord

        log.info "page="+page + " limit="+limit+ " sortedRow="+sortedRow  +" sord="+sord

        if(params.page || params.rows || params.sidx || params.sord) {
          int pg = Integer.parseInt(page)-1
          int max = Integer.parseInt(limit)
          int offset = pg * max

          String filenameSearch = params.filename ?: ""
          Date dateAddedStart = params.createdstart ? new Date(Long.parseLong(params.createdstart)) : new Date(0)
          Date dateAddedStop = params.createdstop ? new Date(Long.parseLong(params.createdstop)) : new Date(8099,11,31) //bad code...another way to keep the max date?

          log.info "filenameSearch="+filenameSearch + " dateAddedStart="+dateAddedStart+ " dateAddedStop="+dateAddedStop

          PagedResultList results = user.abstractimage(max,offset,sortedRow,sord,filenameSearch,dateAddedStart,dateAddedStop)
          data.page = page+""
          data.records = results.totalCount
          data.total =  Math.ceil(results.totalCount/max)+"" //[100/10 => 10 page] [5/15
          data.rows = results.list
        }
        else {
           data = user?.abstractimages()
        }

        if(user!=null) responseSuccess(data)
        else responseNotFound("User",params.id)
      }

    def show = {
        AbstractImage image = AbstractImage.read(params.id)
        if(image) responseSuccess(image)
        else responseNotFound("Image",params.id)
    }


    def listByProject = {
        Project project = Project.read(params.id)
        if(project) responseSuccess(project.abstractimages())
        else responseNotFound("Image","Project",params.id)
    }

    def add = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new AddAbstractImageCommand(user: currentUser), json)
        response(result)
    }

    def update = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new EditAbstractImageCommand(user: currentUser), json)
        response(result)
    }

    def delete = {
        def json = ([id : params.id]) as JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new DeleteAbstractImageCommand(user: currentUser), json)
        response(result)
    }

    def metadata = {
        //TODO; refactor me!
        AbstractImage image = AbstractImage.read(params.id)
        println "URL : " +  image.getMetadataURL()
        def url = new URL(image.getMetadataURL())

        withFormat {
            json {
                render(contentType: "application/json", text: "${url.text}")
            }
        }
    }

    def imageProperties = {
        //TODO; refactor me!
        AbstractImage image = AbstractImage.read(params.id)
        if (image.imageProperties.size() == 0) {
             imagePropertiesService.populate(image)
        }
        response(image.imageProperties)
    }

    def imageProperty = {
        def imageProperty = ImageProperty.findById(params.imageproperty)
        if(imageProperty!=null) responseSuccess(imageProperty)
        else responseNotFound("ImageProperty",params.imageproperty)
    }


    def imageservers = {
        AbstractImage image = AbstractImage.read(params.id)
        def urls = image.getImageServers().collect { it.getZoomifyUrl() + image.getPath() + "/" }
        def result = [:]
        result.imageServersURLs =  urls
        response(result)
    }

    def thumb = {
        AbstractImage image = AbstractImage.read(params.id)
        try {
            responseImage(image.getThumbURL())
        } catch ( Exception e) {
            log.error("GetThumb:"+e);
        }
    }

    def crop = {
        Annotation annotation = Annotation.read(params.id)
        def zoom
        if (params.zoom != null) zoom = Integer.parseInt(params.zoom)
        if(annotation==null)
            responseNotFound("Crop","Annotation",params.id)
        else if((params.zoom != null) && (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max))
            responseNotFound("Crop","Zoom",zoom)
        else
        {
            try {
                if(params.zoom!=null)
                    responseImage(annotation.getCropURL(zoom))
                else
                    responseImage(annotation.getCropURL())
            } catch ( Exception e) {
                log.error("GetThumb:"+e);
            }
        }
    }

    def retrieval = {
        Annotation annotation = Annotation.read(params.idannotation)
        int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getImage().getZoomLevels().middle

        if(annotation==null)
            responseNotFound("Crop","Annotation",params.id)
        else if(zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max)
            responseNotFound("Crop","Zoom",zoom)
        else {

            int maxSimilarPictures = Integer.parseInt(params.maxsimilarpictures)
            def retrievalServers = RetrievalServer.findAll()
            log.debug "annotation.getCropURL(1)=" + annotation.getCropURL(1)
            def list = retrievalServers.get(0).search(annotation.getCropURL(zoom), maxSimilarPictures)
            responseSuccess(list)
        }
    }
}



