package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.project.Slide
import be.cytomine.server.resolvers.Resolver
import grails.converters.JSON
import be.cytomine.image.acquisition.Instrument

class AbstractImage extends CytomineDomain {

    def imagePropertiesService

    String originalFilename
    String filename
    Instrument scanner
    Slide slide
    String path
    Mime mime
    Integer width
    Integer height
    Integer magnification
    Double resolution

    /*
    * If you modify/add an attribute, don't forget to:
    * -Update getXXXXFromData
    * -Update registerMarshaller
    * -Update functionnal test (add/edit test)
    */

    public beforeInsert() {
        super.beforeInsert()
        if (originalFilename == null || originalFilename == "") {
            String filename = getFilename()
            filename = filename.replace(".vips.tiff", "")
            filename = filename.replace(".vips.tif", "")
            if (filename.lastIndexOf("/") != -1 && filename.lastIndexOf("/") != filename.size())
                filename = filename.substring(filename.lastIndexOf("/")+1, filename.size())
            originalFilename = filename
        }


    }
    static belongsTo = Slide

    static hasMany = [abstractimagegroup: AbstractImageGroup, storageAbstractImages: StorageAbstractImage, imageProperties: ImageProperty]

    static transients = ["zoomLevels", "thumbURL"]

    static constraints = {
        originalFilename(nullable: true, blank: false, unique: false)
        filename(blank: false, unique: true)

        scanner(nullable: true)
        slide(nullable: true)

        path(nullable: false)
        mime(nullable: false)

        width(nullable: true)
        height(nullable: true)
        resolution(nullable: true)
        magnification(nullable: true)
    }


    def groups() {
        return abstractimagegroup.collect {
            it.group
        }
    }

    static AbstractImage createFromDataWithId(json) throws CytomineException {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static AbstractImage createFromData(jsonImage) throws CytomineException {
        def image = new AbstractImage()
        getFromData(image, jsonImage)
    }

    static AbstractImage getFromData(image, jsonImage) throws CytomineException {
        image.filename = jsonImage.filename
        image.path = jsonImage.path

        image.height = (!jsonImage.height.toString().equals("null")) ? ((String) jsonImage.height).toInteger() : -1
        image.width = (!jsonImage.width.toString().equals("null")) ? ((String) jsonImage.width).toInteger() : -1
        //image.scale = (!jsonImage.scale.toString().equals("null"))  ? ((String)jsonImage.scale).toDouble() : -1

        image.created = (!jsonImage.created.toString().equals("null")) ? new Date(Long.parseLong(jsonImage.created)) : null
        image.updated = (!jsonImage.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonImage.updated)) : null

        String scannerId = jsonImage.scanner.toString()
        if (!scannerId.equals("null")) {
            image.scanner = Instrument.get(scannerId)
            if (image.scanner == null) throw new WrongArgumentException("Scanner was not found with id:" + scannerId)
        }
        else image.scanner = null

        String slideId = jsonImage.slide.toString()
        if (!slideId.equals("null")) {
            image.slide = Slide.get(slideId)
            if (image.slide == null) throw new WrongArgumentException("Slide was not found with id:" + slideId)
        }
        else image.slide = null

        String mimeId = jsonImage.mime.toString()
        image.mime = Mime.findByExtension(mimeId)
        if (image.mime == null) {
            throw new WrongArgumentException("Mime was not found with id:" + mimeId)
        }
        else if (image.mime.imageServers().size() == 0) {
            throw new WrongArgumentException("Mime with id:" + mimeId + " has not image server")
        }

        return image;
    }

    static void registerMarshaller(String cytomineBaseUrl) {

        println "Register custom JSON renderer for " + AbstractImage.class
        JSON.registerObjectMarshaller(AbstractImage) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['filename'] = it.filename
            returnArray['originalFilename'] = it.originalFilename
            returnArray['scanner'] = it.scanner?.id
            returnArray['slide'] = it.slide?.id
            returnArray['path'] = it.path
            returnArray['mime'] = it.mime.extension
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null
            returnArray['width'] = it.width
            returnArray['height'] = it.height
            returnArray['depth'] = it.getZoomLevels()?.max
            returnArray['resolution'] = it.resolution
            returnArray['magnification'] = it.magnification
            returnArray['thumb'] = it.getThumbURL()
            returnArray['metadataUrl'] = UrlApi.getMetadataURLWithImageId(cytomineBaseUrl,it.id)
            return returnArray
        }
    }

    def getImageServers() {
        if (this.storageAbstractImages != null && this.storageAbstractImages.size() > 0) {
            def imageServers = ImageServer.createCriteria().list {
                eq("available", true)
                inList("storage", this.storageAbstractImages.collect { it.storage })
            }
            return imageServers
        }
        return null
    }

    def getPreviewURL() {
        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0) {
            return "images/cytomine.jpg"
        }
        def index = (Integer) Math.round(Math.random() * (imageServers.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getPreviewUrl(imageServers[index].getBaseUrl(), imageServers[index].getStorage().getBasePath() + getPath())
        return url
    }

    def getThumbURL() {
        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0 || getWidth() == null || getHeight() == null) {
            return "images/cytomine.jpg"
        }
        def index = (Integer) Math.round(Math.random() * (imageServers.size() - 1)) //select an url randomly
        Integer desiredWidth = this.getWidth()
        while (desiredWidth > 512) {
            desiredWidth /= 2
        }

        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getThumbUrl(imageServers[index].getBaseUrl(), imageServers[index].getStorage().getBasePath() + getPath(), desiredWidth)
        return url
    }

    def getMetadataURL() {
        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0) {
            return [] as JSON
        }
        def index = (Integer) Math.round(Math.random() * (imageServers.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getMetaDataURL(imageServers[index].getBaseUrl(), imageServers[index].getStorage().getBasePath() + getPath())
        return url
    }

    def getPropertiesURL() {
        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0) {
            return [] as JSON
        }
        def index = (Integer) Math.round(Math.random() * (imageServers.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getPropertiesURL(imageServers[index].getBaseUrl(), imageServers[index].getStorage().getBasePath() + getPath())
        return url
    }

    def getCropURLWithMaxWithOrHeight(int topLeftX, int topLeftY, int width, int height, int dimension) {
        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0) {
            return "images/cytomine.jpg"
        }
        def index = (Integer) Math.round(Math.random() * (imageServers.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        def baseUrl = imageServers[index].getBaseUrl()
        def storage = imageServers[index].getStorage()
        def basePath = storage.getBasePath()
        def path = getPath()
        def widthImg =  this.getWidth()
        def heightImg = this.getHeight()
        resolver.getCropURL(baseUrl, basePath + path, topLeftX, topLeftY, width, height, widthImg,heightImg, dimension)
    }

    def getCropURL(int topLeftX, int topLeftY, int width, int height) {
        getCropURLWithMaxWithOrHeight(topLeftX, topLeftY, width, height, 5000)
    }

    def getCropURL(int topLeftX, int topLeftY, int width, int height, int zoom) {
        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0) {
            return "images/cytomine.jpg"
        }
        def index = (Integer) Math.round(Math.random() * (imageServers.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getCropURL(imageServers[index].getBaseUrl(), imageServers[index].getStorage().getBasePath() + getPath(), topLeftX, topLeftY, width, height, zoom, this.getWidth(), this.getHeight())
        return url
    }

    def getZoomLevels() {

        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0 || width == null || height == null) return null
        Resolver resolver = Resolver.getResolver(imageServers[0].className)
        return resolver.getZoomLevels(imageServers[0].getBaseUrl(), imageServers[0].getStorage().getBasePath() + getPath(), width, height)
    }
}