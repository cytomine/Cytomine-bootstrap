package be.cytomine.image


import be.cytomine.Exception.CytomineException
import be.cytomine.command.*
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON
import grails.orm.PagedResultList
import ij.ImagePlus

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class AbstractImageService extends ModelService {

    static transactional = false

    def commandService
    def cytomineService
    def imagePropertiesService
    def transactionService
    def storageService
    def groupService
    def imageInstanceService

    def currentDomain() {
        return AbstractImage
    }

    //TODO: secure! ACL
    AbstractImage read(def id) {
        AbstractImage abstractImage = AbstractImage.read(id)
        abstractImage
    }

    //TODO: secure! ACL
    AbstractImage get(def id) {
        return AbstractImage.get(id)
    }

    //TODO: secure!
    def list(Project project) {
        ImageInstance.createCriteria().list {
            eq("project", project)
            projections {
                groupProperty("baseImage")
            }
        }
    }

    //TODO: secure! ACL
    def list(User user) {
        if(user.admin) {
            return AbstractImage.list()
        } else {
            def allImages = []
            def groups = groupService.list(user)
            groups.each { group ->
                allImages.addAll(list(group))

            }
            return allImages
        }
    }

    //TODO: secure! ACL
    def list(SecUser user, def page, def limit, def sortedRow, def sord, def filename, def dateStart, def dateStop) {
        def data = [:]

        log.info "page=" + page + " limit=" + limit + " sortedRow=" + sortedRow + " sord=" + sord

        if (page || limit || sortedRow || sord) {
            int pg = Integer.parseInt(page) - 1
            int max = Integer.parseInt(limit)
            int offset = pg * max

            String filenameSearch = filename!=null ? filename : ""
            Date dateAddedStart = dateStart!=null && dateStart!="" ? new Date(Long.parseLong(dateStart)) : new Date(0)
            Date dateAddedStop = dateStop!=null && dateStop!="" ? new Date(Long.parseLong(dateStop)) : new Date(8099, 11, 31) //another way to keep the max date?

            log.info "filenameSearch=" + filenameSearch + " dateAddedStart=" + dateAddedStart + " dateAddedStop=" + dateAddedStop

            def abstractImages = StorageAbstractImage.findAllByStorageInList(storageService.list()).collect { it.abstractImage.id }

            if(!abstractImages.isEmpty())   {
                log.info "${abstractImages.size()} offset=$offset max=$max sortedRow=$sortedRow sord=$sord filename=%$filenameSearch% created $dateAddedStart < $dateAddedStop"
                PagedResultList results = AbstractImage.createCriteria().list(offset: offset, max: max, sort: sortedRow, order: sord) {
                    inList("id", abstractImages)
                    ilike("filename", "%" + filenameSearch + "%")
                    between('created', dateAddedStart, dateAddedStop)

                }
                data.page = page + ""
                data.records = results.totalCount
                data.total = Math.ceil(results.totalCount / max) + "" //[100/10 => 10 page] [5/15
                data.rows = results.list
                /*} else {
                    //GORM GOTCHA: list in inList cannot be empty
                    data.page = page + ""
                    data.records = 0
                    data.total = 0
                    data.rows = []
                }*/
            }
        }
        return data
    }


    //TODO:: how to manage security here?
    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new AddCommand(user: currentUser)
        def res = executeCommand(c,null,json)
        //AbstractImage abstractImage = retrieve(res.data.abstractimage)
        AbstractImage abstractImage = res.object
        Group group = Group.findByName(currentUser.getUsername())

        json.storage.each { storageID ->
            Storage storage = storageService.read(storageID)
            StorageAbstractImage sai = new StorageAbstractImage(storage:storage,abstractImage:abstractImage)
            sai.save(flush:true,failOnError: true)
        }
        imagePropertiesService.extractUseful(abstractImage)
        abstractImage.save(flush : true)
        //Stop transaction

        return res
    }

    //TODO:: how to manage security here?
    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(AbstractImage image,def jsonNewData) throws CytomineException {
        transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        def res = executeCommand(new EditCommand(user: currentUser), image,jsonNewData)
        AbstractImage abstractImage = res.object
        StorageAbstractImage.findAllByAbstractImage(abstractImage).each { storageAbstractImage ->
            def sai = StorageAbstractImage.findByStorageAndAbstractImage(storageAbstractImage.storage, abstractImage)
            sai.delete(flush:true)
        }
        jsonNewData.storage.each { storageID ->
            Storage storage = storageService.read(storageID)
            StorageAbstractImage sai = new StorageAbstractImage(storage:storage,abstractImage:abstractImage)
            sai.save(flush:true,failOnError: true)
        }

        return res
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(AbstractImage domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


    /**
     * Get Image metadata
     */
    def metadata(def id) {
        AbstractImage image = read(id)
        def url = image.getMetadataURL()
        println url
        url = new URL(url)
        return url.text
    }

    /**
     * Extract image properties from file for a specific image
     */
    def imageProperties(def id) {
        AbstractImage image = read(id)
        if (!ImageProperty.findByImage(image)) {
            imagePropertiesService.populate(image)
        }
        return ImageProperty.findAllByImage(image)
    }

    /**
     * Get a single property thx to its id
     */
    def imageProperty(def imageProperty) {
        return ImageProperty.findById(imageProperty)
    }

    /**
     * Get all image servers for an image id
     */
    def imageServers(def id) {
        AbstractImage image = read(id)
        def urls = []
        for (imageServerStorage in image.getImageServersStorage()) {
            urls << [imageServerStorage.getZoomifyUrl(), image.getPath()].join(File.separator) + "/"
        }
        return [imageServersURLs : urls]
    }

    /**
     * Get thumb image URL
     */
    def thumb(def id) {
        AbstractImage image = AbstractImage.read(id)
        try {
            return image.getThumbURL()
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
    }

    /**
     * Get Preview image URL
     */
    def preview(def id) {
        AbstractImage image = AbstractImage.read(id)
        try {
            String previewURL = image.getPreviewURL()
            if (previewURL == null) previewURL = grailsApplication.config.grails.serverURL + "/images/cytomine.jpg"
            return previewURL
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    def downloadURI(def id) {
        String imageServerURL = grailsApplication.config.grails.imageServerURL
        return "$imageServerURL/api/abstractimage/$id/download"
    }

    def getAvailableAssociatedImages(def id) {
        String imageServerURL = grailsApplication.config.grails.imageServerURL
        String uri = "$imageServerURL/api/abstractimage/$id/associated"
        return JSON.parse( new URL(uri).text )
    }

    def getAssociatedImage(def id, String label, def maxWidth) {
        AbstractImage abstractImage = read(id)
        AssociatedImage associatedImage = AssociatedImage.findByAbstractImageAndLabel(abstractImage, label)
        if (associatedImage) {
            return ImageIO.read(new ByteArrayInputStream(associatedImage.getImageData()))
        } else {
            String imageServerURL = grailsApplication.config.grails.imageServerURL
            def queryString = ""
            if (maxWidth) {
                queryString = "maxWidth=$maxWidth"
            }
            def uri = "$imageServerURL/api/abstractimage/$id/associated/$label?$queryString"
            byte[] imageData = new URL(uri).getBytes()
            BufferedImage bufferedImage =  ImageIO.read(new ByteArrayInputStream(imageData))
            new AssociatedImage( abstractImage: abstractImage, label : label, imageData: imageData).save(flush : true)
            return bufferedImage
        }

    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.filename]
    }

    def deleteDependentImageInstance(AbstractImage ai, Transaction transaction,Task task=null) {
        ImageInstance.findAllByBaseImage(ai).each {
            imageInstanceService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentImageProperty(AbstractImage ai, Transaction transaction,Task task=null) {
        //TODO: implement imagePropertyService with command
        imagePropertiesService.clear(ai)
    }

    def deleteDependentNestedFile(AbstractImage ai, Transaction transaction,Task task=null) {
        //TODO: implement this with command (nestedFileService should be create)
        NestedFile.findAllByAbstractImage(ai).each {
            it.delete(flush: true)
        }
    }

    def deleteDependentStorageAbstractImage(AbstractImage ai, Transaction transaction,Task task=null) {
        //TODO: implement this with command (storage abst image should be create)
        StorageAbstractImage.findAllByAbstractImage(ai).each {
            it.delete(flush: true)
        }
    }

    def deleteDependentNestedImageInstance(AbstractImage ai, Transaction transaction,Task task=null) {
        NestedImageInstance.findAllByBaseImage(ai).each {
            it.delete(flush: true)
        }
    }
}
