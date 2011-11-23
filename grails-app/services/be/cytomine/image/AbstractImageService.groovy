package be.cytomine.image

import be.cytomine.Exception.CytomineException
import be.cytomine.ModelService
import be.cytomine.command.abstractimage.AddAbstractImageCommand
import be.cytomine.command.abstractimage.DeleteAbstractImageCommand
import be.cytomine.command.abstractimage.EditAbstractImageCommand
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.RetrievalServer
import be.cytomine.ontology.Annotation
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.User
import grails.orm.PagedResultList

class AbstractImageService extends ModelService {

    static transactional = false
    def commandService
    def cytomineService
    def imagePropertiesService

    def list() {
        return AbstractImage.list()
    }

    AbstractImage read(def id) {
        return AbstractImage.read(id)
    }

    AbstractImage get(def id) {
        return AbstractImage.get(id)
    }

    def list(Project project) {
        project.abstractimages()
    }

    def list(Group group) {
        group.abstractimages()
    }

    def list(User user, def page, def limit, def sortedRow, def sord, def filename, def dateStart, def dateStop) {
        def data = [:]

        log.info "page=" + page + " limit=" + limit + " sortedRow=" + sortedRow + " sord=" + sord

        if (page || limit || sortedRow || sord) {
            int pg = Integer.parseInt(page) - 1
            int max = Integer.parseInt(limit)
            int offset = pg * max

            String filenameSearch = filename ?: ""
            Date dateAddedStart = dateStart ? new Date(Long.parseLong(dateStart)) : new Date(0)
            Date dateAddedStop = dateStop ? new Date(Long.parseLong(dateStop)) : new Date(8099, 11, 31) //bad code...another way to keep the max date?

            log.info "filenameSearch=" + filenameSearch + " dateAddedStart=" + dateAddedStart + " dateAddedStop=" + dateAddedStop

            PagedResultList results = user.abstractimage(max, offset, sortedRow, sord, filenameSearch, dateAddedStart, dateAddedStop)
            data.page = page + ""
            data.records = results.totalCount
            data.total = Math.ceil(results.totalCount / max) + "" //[100/10 => 10 page] [5/15
            data.rows = results.list
        }
        else {
            data = user?.abstractimages()
        }
        return data
    }


    def add(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new AddAbstractImageCommand(user: currentUser), json)
    }

    def update(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new EditAbstractImageCommand(user: currentUser), json)
        return result
    }

    def delete(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new DeleteAbstractImageCommand(user: currentUser), json)
    }


    def metadata(def id) {
        AbstractImage image = AbstractImage.read(id)
        def url = new URL(image.getMetadataURL())
        return url.text
    }

    def imageProperties(def id) {
        AbstractImage image = AbstractImage.read(id)
        if (image.imageProperties.isEmpty()) {
            imagePropertiesService.populate(image)
        }
        return image.imageProperties
    }

    def imageProperty(def imageproperty) {
        return ImageProperty.findById(imageproperty)
    }


    def imageservers(def id) {
        AbstractImage image = AbstractImage.read(id)
        def urls = image.getImageServers().collect { it.getZoomifyUrl() + image.getPath() + "/" }
        def result = [:]
        result.imageServersURLs = urls
        return result
    }

    def thumb(def id) {
        AbstractImage image = AbstractImage.read(id)
        try {
            return image.getThumbURL()
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
    }

    def crop(Annotation annotation, def zoom) {
        if (zoom)
            return annotation.getCropURL(zoom)
        else
            return annotation.getCropURL()
    }

    def retrieval(Annotation annotation, int zoom, int maxSimilarPictures) {
        def retrievalServers = RetrievalServer.findAll()
        return retrievalServers.get(0).search(annotation.getCropURL(zoom), maxSimilarPictures)
    }
}
