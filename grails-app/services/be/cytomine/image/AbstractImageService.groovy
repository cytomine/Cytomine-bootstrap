package be.cytomine.image

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import grails.orm.PagedResultList
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.annotation.Secured
import be.cytomine.command.Transaction
import grails.converters.JSON
import be.cytomine.utils.Task

class AbstractImageService extends ModelService {

    static transactional = false

    def commandService
    def cytomineService
    def imagePropertiesService
    def transactionService
    def storageService
    def abstractImageGroupService
    def groupService
    def imageInstanceService

    def currentDomain() {
        return AbstractImage
    }

    /**
     * List all images (only for admin!)
     */
    @Secured(['ROLE_ADMIN'])
    def list() {
        return AbstractImage.list()
    }

    //TODO: secure! ACL
    AbstractImage read(def id) {
        AbstractImage abstractImage = AbstractImage.read(id)
        if (abstractImage && !abstractImage) {

            //SecurityCheck.checkReadAuthorization(image.project)
        }
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

    //TODO: secure!
    def list(Group group) {
        AbstractImageGroup.findAllByGroup(group).collect{
            it.abstractImage
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

            def userGroup = abstractImageGroupService.list(user)
            log.info "userGroup=" + userGroup.size()
            def imageGroup = AbstractImageGroup.createCriteria().list {
                inList("group.id", userGroup.collect {it.group.id})
                projections {
                    groupProperty('abstractImage.id')
                }
            }
            log.info "imageGroup=" + imageGroup.size()

            log.info "offset=$offset max=$max sortedRow=$sortedRow sord=$sord filename=%$filenameSearch% created $dateAddedStart < $dateAddedStop"
            PagedResultList results = AbstractImage.createCriteria().list(offset: offset, max: max, sort: sortedRow, order: sord) {
                inList("id", imageGroup)
                ilike("filename", "%" + filenameSearch + "%")
                between('created', dateAddedStart, dateAddedStop)

            }
            data.page = page + ""
            data.records = results.totalCount
            data.total = Math.ceil(results.totalCount / max) + "" //[100/10 => 10 page] [5/15
            data.rows = results.list
        }
        else {
            data = list(user)
        }
        return data
    }


    //TODO:: how to manage security here?
    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    def add(def json,SecurityCheck security) throws CytomineException {
        transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        def res = executeCommand(new AddCommand(user: currentUser), json)
        //AbstractImage abstractImage = retrieve(res.data.abstractimage)
        AbstractImage abstractImage = res.object
        Group group = Group.findByName(currentUser.getUsername())

        AbstractImageGroup aig = new AbstractImageGroup(abstractImage: abstractImage,group:group)
        aig.save(flush:true,failOnError: true)

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
     * @param security Security service object (user for right check)
     * @param json JSON with new data
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(def json,SecurityCheck security) throws CytomineException {
        transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        def res = executeCommand(new EditCommand(user: currentUser), json)
        AbstractImage abstractImage = res.object
        StorageAbstractImage.findAllByAbstractImage(abstractImage).each { storageAbstractImage ->
            def sai = StorageAbstractImage.findByStorageAndAbstractImage(storageAbstractImage.storage, abstractImage)
            sai.delete(flush:true)
        }
        json.storage.each { storageID ->
            Storage storage = storageService.read(storageID)
            StorageAbstractImage sai = new StorageAbstractImage(storage:storage,abstractImage:abstractImage)
            sai.save(flush:true,failOnError: true)
        }

        return res
    }

    //TODO:: how to manage security here?
    /**
     * Delete domain in argument
     * @param security Security service object (user for right check)
     * @param json JSON that was passed in request parameter
     * @return Response structure (created domain data,..)
     */
    def delete(def json,SecurityCheck security, Task task = null) throws CytomineException {
//        transactionService.start()
//        SecUser currentUser = cytomineService.getCurrentUser()
//        AbstractImage abstractImage = read(json.id)
//        Group group = Group.findByName(currentUser.getUsername())
//        AbstractImageGroup.unlink(abstractImage, group)
//        StorageAbstractImage.findAllByAbstractImage(abstractImage).each { storageAbstractImage ->
//            StorageAbstractImage.unlink(storageAbstractImage.storage, storageAbstractImage.abstractImage)
//        }
//
//        ImageProperty.findAllByImage(abstractImage).each {
//            it.delete(flush:true)
//        }
//
//        def res =  executeCommand(new DeleteCommand(user: currentUser), json)
        return delete(retrieve(json),transactionService.start())
    }

    def delete(AbstractImage image, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${image.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }


    /**
     * Get Image metadata
     */
    def metadata(def id) {
        AbstractImage image = read(id)
        def url = new URL(image.getMetadataURL())
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
        println "thumb=$id"
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

    /**
     * Get annotation crop from this image
     */
    def cropWithMaxSize(AnnotationDomain annotation, int maxSize) {
        return annotation.toCropURLWithMaxSize(maxSize)
    }

    /**
     * Get annotation crop from this image
     */
    def crop(AnnotationDomain annotation, Integer zoom) {
        def boundaries = annotation.getBoundaries()
        if (zoom != null) {
            int desiredWidth = boundaries.width / Math.pow(2, zoom)
            int desiredHeight = boundaries.height / Math.pow(2, zoom)
            return cropWithMaxSize(annotation, Math.max(desiredHeight, desiredWidth))
        } else {
            return annotation.toCropURL()
        }
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.filename]
    }

    def deleteDependentAbstractImageGroup(AbstractImage ai, Transaction transaction,Task task=null) {
        if(task) {
            def nb = AbstractImageGroup.countByAbstractImage(ai)
            taskService.updateTask(task,"Delete $nb link to group")
        }

        AbstractImageGroup.findAllByAbstractImage(ai).each {
            abstractImageGroupService.delete(it,transaction,false)
        }
    }

    def deleteDependentImageInstance(AbstractImage ai, Transaction transaction,Task task=null) {
        ImageInstance.findAllByBaseImage(ai).each {
            imageInstanceService.delete(it,transaction,false)
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




}
