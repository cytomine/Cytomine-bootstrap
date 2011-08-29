package be.cytomine.api.project

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

class RestImageController extends RestController{

    def springSecurityService
    def transactionService
    def imagePropertiesService

    def index = {
        redirect(controller: "image")
    }
    def list = {
        log.info "list"
        response(AbstractImage.list())
    }

  /*def listByUser = {
    log.info "List with id user:"+params.id
    User user=null
    if(params.id!=null) {
      user = User.read(params.id)
    } else {
       user = getCurrentUser(springSecurityService.principal.id)
    }

    if(user!=null) responseSuccess(user.abstractimages())
    else responseNotFound("User",params.id)
  }*/

      def listByUser = {
        log.info "List with id user:"+params.id
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
        log.info "show " + params.id
        AbstractImage image = AbstractImage.read(params.id)
        if(image!=null) responseSuccess(image)
        else responseNotFound("Image",params.id)
    }


    def listByProject = {
        log.info "List with id user:"+params.id
        Project project = Project.read(params.id)
        if(project!=null) responseSuccess(project.abstractimages())
        else responseNotFound("Image","Project",params.id)
    }

    def add = {
        log.info "Add"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command addImageCommand = new AddAbstractImageCommand(postData : request.JSON.toString(), user: currentUser)
        def result = processCommand(addImageCommand, currentUser)
        response(result)
    }

    def update = {
        log.info "Update"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command editImageCommand = new EditAbstractImageCommand(postData : request.JSON.toString(), user: currentUser)
        def result = processCommand(editImageCommand, currentUser)
        response(result)
    }

    def delete = {
        log.info "Delete"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " params.id=" + params.id
        def postData = ([id : params.id]) as JSON
        Command deleteImageCommand = new DeleteAbstractImageCommand(postData : postData.toString(), user: currentUser)
        def result = processCommand(deleteImageCommand, currentUser)
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
    def storageService

    def imageservers = {
        AbstractImage image = AbstractImage.read(params.id)
        def urls = image.getMime().imageServers().collect { it.getZoomifyUrl() + image.getPath() + "/" }
        def result = [:]
        result.imageServersURLs =  urls
        response(result)
    }

    def thumb = {
        log.info "Thumb with id:" + params.id
        AbstractImage image = AbstractImage.read(params.id)
        log.info "image.getThumbURL()="+image.getThumbURL()
        try {
            responseImage(image.getThumbURL())
        } catch ( Exception e) {
            log.error("GetThumb:"+e);
        }
    }

    def crop = {
        log.info "Crop with id annotation: " + params.id
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



