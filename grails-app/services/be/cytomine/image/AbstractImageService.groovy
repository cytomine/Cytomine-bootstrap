package be.cytomine.image

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.command.*
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.AttachedFile
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LinearRing
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import grails.orm.PagedResultList
import org.codehaus.groovy.grails.plugins.codecs.URLCodec

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.springframework.security.acls.domain.BasePermission.*

class AbstractImageService extends ModelService {

    static transactional = false

    def commandService
    def cytomineService
    def imagePropertiesService
    def transactionService
    def storageService
    def groupService
    def imageInstanceService
    def attachedFileService
    def currentRoleServiceProxy
    def securityACLService

    def currentDomain() {
        return AbstractImage
    }

    AbstractImage read(def id) {
        AbstractImage abstractImage = AbstractImage.read(id)
        if(abstractImage) {
            //securityACLService.checkAtLeastOne(abstractImage, READ)
            if(!hasRightToReadAbstractImageWithProject(abstractImage) && !hasRightToReadAbstractImageWithStorage(abstractImage)) {
                throw new ForbiddenException("You don't have the right to read or modity this resource! ${abstractImage} ${id}")
            }
        }
        abstractImage
    }

    AbstractImage get(def id) {
        AbstractImage abstractImage = AbstractImage.get(id)
        if(abstractImage) {
            //securityACLService.checkAtLeastOne(abstractImage, READ)
            if(!hasRightToReadAbstractImageWithProject(abstractImage) && !hasRightToReadAbstractImageWithStorage(abstractImage)) {
                throw new ForbiddenException("You don't have the right to read or modity this resource! ${abstractImage} ${id}")
            }
        }
        abstractImage
    }

    boolean hasRightToReadAbstractImageWithProject(AbstractImage image) {
        List<ImageInstance> imageInstances = ImageInstance.findAllByBaseImage(image)
        List<Project> projects = imageInstances.collect{it.project}
        for(Project project : projects) {
            if(project.hasACLPermission(project,READ)) return true
        }
        return false
    }

    boolean hasRightToReadAbstractImageWithStorage(AbstractImage image) {
        List<Storage> storages = StorageAbstractImage.findAllByAbstractImage(image).collect{it.storage}
        for(Storage storage : storages) {
            if(storage.hasACLPermission(storage,READ)) return true
        }
        return false
    }

    def list(Project project) {
        securityACLService.check(project,READ)
        ImageInstance.createCriteria().list {
            eq("project", project)
            projections {
                groupProperty("baseImage")
            }
        }
    }

    def list(User user) {
        if(currentRoleServiceProxy.isAdminByNow(user)) {
            return AbstractImage.list()
        } else {
            List<Storage> storages = securityACLService.getStorageList(cytomineService.currentUser)
            List<AbstractImage> images = StorageAbstractImage.findAllByStorageInList(storages).collect{it.abstractImage}
            return images
        }
    }

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

        json.storage.each { storageID ->
            Storage storage = storageService.read(storageID)
            securityACLService.check(storage,WRITE)
            //CHECK WRITE ON STORAGE
            StorageAbstractImage sai = new StorageAbstractImage(storage:storage,abstractImage:abstractImage)
            sai.save(flush:true,failOnError: true)
        }
        imagePropertiesService.extractUseful(abstractImage)
        abstractImage.save(flush : true)
        //Stop transaction

        return res
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(AbstractImage image,def jsonNewData) throws CytomineException {
        securityACLService.checkAtLeastOne(image,WRITE)
        transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        def res = executeCommand(new EditCommand(user: currentUser), image,jsonNewData)
        AbstractImage abstractImage = res.object

        if(jsonNewData.storage) {
            StorageAbstractImage.findAllByAbstractImage(abstractImage).each { storageAbstractImage ->
                securityACLService.check(storageAbstractImage.storage,WRITE)
                def sai = StorageAbstractImage.findByStorageAndAbstractImage(storageAbstractImage.storage, abstractImage)
                sai.delete(flush:true)
            }
            jsonNewData.storage.each { storageID ->
                Storage storage = storageService.read(storageID)
                securityACLService.check(storage,WRITE)
                StorageAbstractImage sai = new StorageAbstractImage(storage:storage,abstractImage:abstractImage)
                sai.save(flush:true,failOnError: true)
            }
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
        securityACLService.checkAtLeastOne(domain,WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


    def crop(params, queryString) {
        queryString = queryString.replace("?", "")
        AbstractImage abstractImage = read(params.id)
        String imageServerURL = abstractImage.getRandomImageServerURL()
        UploadedFile uploadedFile = getMainUploadedFile(abstractImage)
        String fif = URLEncoder.encode(uploadedFile.absolutePath, "UTF-8")
        String mimeType = uploadedFile.mimeType
        return "$imageServerURL/image/crop.png?fif=$fif&mimeType=$mimeType&$queryString&resolution=${abstractImage.resolution}" //&scale=$scale
    }

    def tile(def params, String queryString) {
        log.info "tile request"
        AbstractImage abstractImage = read(params.id)
        int tileGroup = params.int("TileGroup")
        int x = params.int("x")
        int y = params.int("y")
        int z = params.int("z")
        String imageServerURL = abstractImage.getRandomImageServerURL()
        UploadedFile uploadedFile = getMainUploadedFile(abstractImage)
        String fif = URLEncoder.encode(uploadedFile.absolutePath, "UTF-8")
        String mimeType = uploadedFile.mimeType
        def zoomifyQuery = "zoomify=$fif/TileGroup$tileGroup/$z-$x-$y\\.jpg&mimeType=$mimeType"
        return "$imageServerURL/image/tile.jpg?$zoomifyQuery"
    }

    def window(def params, String queryString, Long width = null, Long height = null) {
        Long id = params.long('id')
        AbstractImage abstractImage = read(id)
        int x = params.int('x')
        int y = params.int('y')
        int w = params.int('w')
        int h = params.int('h')
        def parameters = [:]
        parameters.topLeftX = Math.max(x,0)
        parameters.topLeftY = Math.max(abstractImage.getHeight() - y,0)
        parameters.width = w
        parameters.height = h
        parameters.imageWidth = abstractImage.getWidth()
        parameters.imageHeight = abstractImage.getHeight()

        if(width && (parameters.width+parameters.topLeftX)>width) {
            //for camera, don't take the part outsite the real image
            parameters.width = width - parameters.topLeftX
        }
//        if(height && (parameters.height+parameters.topLeftY)>height) {
//            //for camera, don't take the part outsite the real image
//            parameters.height = height - parameters.topLeftY
//        }

        if (params.zoom) parameters.zoom = params.zoom
        if (params.maxSize) parameters.maxSize = params.maxSize
        if (params.location) parameters.location = params.location
        if (params.mask) parameters.mask = params.mask
        if (params.alphaMask) parameters.alphaMask = params.alphaMask
        return UrlApi.getCropURL(id, parameters)
    }



//    /**
//     * Extract image properties from file for a specific image
//     */
//    def imageProperties(AbstractImage abstractImage) {
//        if (!ImageProperty.findByImage(abstractImage)) {
//            imagePropertiesService.populate(abstractImage)
//        }
//        return ImageProperty.findAllByImage(abstractImage)
//    }
//
//    /**
//     * Get a single property thx to its id
//     */
//    def imageProperty(long imageProperty) {
//        return ImageProperty.findById(imageProperty)
//    }

    /**
     * Get all image servers for an image id
     */
    def imageServers(def id) {
        AbstractImage image = read(id)
        UploadedFile uploadedFile = getMainUploadedFile(image)
        def urls = []
        for (imageServerStorage in image.getImageServersStorage()) {
            urls << [imageServerStorage.getZoomifyUrl(), image.getPath()].join(File.separator) + "/" //+ "&mimeType=${uploadedFile.mimeType}"
        }


        return [imageServersURLs : urls]
    }

    /**
     * Get thumb image URL
     */
    def thumb(long id, int maxSize) {
        AbstractImage abstractImage = AbstractImage.read(id)
        String imageServerURL = abstractImage.getRandomImageServerURL()
        UploadedFile uploadedFile = getMainUploadedFile(abstractImage)
        String fif = URLEncoder.encode(uploadedFile.absolutePath, "UTF-8")
        String mimeType = uploadedFile.mimeType
        String url = "$imageServerURL/image/thumb.jpg?fif=$fif&mimeType=$mimeType&maxSize=$maxSize"
        AttachedFile attachedFile = AttachedFile.findByDomainIdentAndFilename(id, url)
        if (attachedFile) {
            return ImageIO.read(new ByteArrayInputStream(attachedFile.getData()))
        } else {
            log.info url
            byte[] imageData = new URL(url).getBytes()
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData))
            attachedFileService.add(url, imageData, abstractImage.id, AbstractImage.class.getName())
            return bufferedImage
        }

    }

    /**
     * Get Preview image URL
     */
    def preview(def id) {
        thumb(id, 1024)
    }

    def getMainUploadedFile(AbstractImage abstractImage) {
        UploadedFile uploadedfile = UploadedFile.findByImage(abstractImage)
        if (uploadedfile?.parent && !uploadedfile?.parent?.ext?.equals("png") && !uploadedfile?.parent?.ext?.equals("jpg")) {
            return uploadedfile.parent
        }
        else return uploadedfile

    }

    def downloadURI(AbstractImage abstractImage) {
        List<UploadedFile> files = UploadedFile.findAllByImage(abstractImage)
        UploadedFile file = files.find{it.downloadParent!=null}
        String fif = file?.absolutePath
        if (fif) {
            String imageServerURL = abstractImage.getRandomImageServerURL()
            return "$imageServerURL/image/download?fif=$fif"
        } else {
            return null
        }

    }

    def getAvailableAssociatedImages(AbstractImage abstractImage) {
        String imageServerURL = abstractImage.getRandomImageServerURL()
        UploadedFile uploadedFile = getMainUploadedFile(abstractImage)
        String fif = URLEncoder.encode(uploadedFile.absolutePath, "UTF-8")
        String mimeType = uploadedFile.mimeType
        String url = "$imageServerURL/image/associated.json?fif=$fif&mimeType=$mimeType"
        return JSON.parse( new URL(url).text )
    }

    def getAssociatedImage(AbstractImage abstractImage, String label, def maxWidth) {
        String imageServerURL = abstractImage.getRandomImageServerURL()
        UploadedFile uploadedFile = getMainUploadedFile(abstractImage)
        String fif = URLEncoder.encode(uploadedFile.absolutePath, "UTF-8")
        String mimeType = uploadedFile.mimeType
        String url = "$imageServerURL/image/nested.jpg?fif=$fif&mimeType=$mimeType&label=$label&maxWidth=$maxWidth"
        AttachedFile attachedFile = AttachedFile.findByDomainIdentAndFilename(abstractImage.id, url)
        if (attachedFile) {
            return ImageIO.read(new ByteArrayInputStream(attachedFile.getData()))
        } else {
            byte[] imageData = new URL(url).getBytes()
            BufferedImage bufferedImage =  ImageIO.read(new ByteArrayInputStream(imageData))
            attachedFileService.add(url, imageData, abstractImage.id, AbstractImage.class.getName())
            return bufferedImage
        }

    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.originalFilename]
    }

    def deleteDependentImageInstance(AbstractImage ai, Transaction transaction,Task task=null) {
        def images = ImageInstance.findAllByBaseImage(ai)
        if(!images.isEmpty()) {
            throw new WrongArgumentException("You cannot delete this image, it has already been insert in projects " + images.collect{it.project.name})
        }
    }

    def deleteDependentAttachedFile(AbstractImage ai, Transaction transaction,Task task=null) {
        AttachedFile.findAllByDomainIdentAndDomainClassName(ai.id, ai.class.getName()).each {
            attachedFileService.delete(it,transaction,null,false)
        }
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
