package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.acquisition.Instrument
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.MimeImageServer
import be.cytomine.image.server.StorageAbstractImage

import be.cytomine.server.resolvers.Resolver
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.laboratory.Sample

/**
 * An abstract image is an image that can be map with projects.
 * When an "AbstractImage" is add to a project, a "ImageInstance" is created.
 */
class AbstractImage extends CytomineDomain implements Serializable {

    def imagePropertiesService

    String originalFilename
    String filename
    Instrument scanner
    Sample sample
    String path
    Mime mime
    Integer width
    Integer height
    Integer magnification
    Double resolution

    static belongsTo = Sample

    static hasMany = [abstractimagegroup: AbstractImageGroup, storageAbstractImages: StorageAbstractImage, imageProperties: ImageProperty]

    static transients = ["zoomLevels", "thumbURL"]

    static constraints = {
        originalFilename(nullable: true, blank: false, unique: false)
        filename(blank: false, unique: true)
        scanner(nullable: true)
        sample(nullable: true)
        path(nullable: false)
        mime(nullable: false)
        width(nullable: true)
        height(nullable: true)
        resolution(nullable: true)
        magnification(nullable: true)
    }

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
        image.filename = getJSONAttrStr(jsonImage,'filename')
        image.path = getJSONAttrStr(jsonImage,'path')
        image.height = getJSONAttrInteger(jsonImage,'height',-1)
        image.width = getJSONAttrInteger(jsonImage,'width',-1)
        image.created = getJSONAttrDate(jsonImage,'created')
        image.updated = getJSONAttrDate(jsonImage,'updated')
        image.scanner = getJSONAttrDomain(jsonImage,"scanner",new Instrument(),false)
        image.sample = getJSONAttrDomain(jsonImage,"sample",new Sample(),false)
        image.mime = getJSONAttrDomain(jsonImage,"mime",new Mime(),'extension','String',true)

        if (image.mime.imageServers().size() == 0) {
            throw new WrongArgumentException("Mime with id:${jsonImage.mime} has not image server")
        }
        return image;
    }

    static void registerMarshaller(String cytomineBaseUrl) {

        Logger.getLogger(this).info("Register custom JSON renderer for " + AbstractImage.class)
        JSON.registerObjectMarshaller(AbstractImage) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['filename'] = it.filename
            returnArray['originalFilename'] = it.originalFilename
            returnArray['scanner'] = it.scanner?.id
            returnArray['sample'] = it.sample?.id
            returnArray['path'] = it.path
            returnArray['mime'] = it.mime.extension
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
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
                inList("id", MimeImageServer.findAllByMime(this.getMime()).collect {it.imageServer.id}.unique())
            }
            return imageServers
        }
        return null
    }

    def getPreviewURL() {
        return getCropURLWithMaxWithOrHeight(0, this.height, this.width, this.height, 5096, 5096)
    }

    def getThumbURL() {
        return getCropURLWithMaxWithOrHeight(0, this.height, this.width, this.height, 256, 256)
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

    def getCropURLWithMaxWithOrHeight(def topLeftX, def topLeftY, def width, def height, def desiredWidth, def desiredHeight) {
        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0) {
            return null
        }
        def index = (Integer) Math.round(Math.random() * (imageServers.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        def baseUrl = imageServers[index].getBaseUrl()
        def storage = imageServers[index].getStorage()
        def basePath = storage.getBasePath()
        def path = getPath()
        def widthImg =  this.getWidth()
        def heightImg = this.getHeight()
        resolver.getCropURL(baseUrl, basePath + path, topLeftX, topLeftY, width, height, widthImg,heightImg, desiredWidth, desiredHeight)
    }

    def getCropURL(int topLeftX, int topLeftY, int width, int height) {
        getCropURLWithMaxWithOrHeight(topLeftX, topLeftY, width, height, 5000, (int) (5000 / (width / height)))
    }

    def getCropURL(int topLeftX, int topLeftY, int width, int height, int zoom) {
        int desiredWidth = Math.round(width / Math.pow(2, zoom))
        int desiredHeight= Math.round(height / Math.pow(2, zoom))
        getCropURLWithMaxWithOrHeight(topLeftX, topLeftY, width, height, desiredWidth, desiredHeight)
    }

    def getZoomLevels() {
        def imageServers = getImageServers()
        if (imageServers == null || imageServers.size() == 0 || width == null || height == null) return null
        Resolver resolver = Resolver.getResolver(imageServers[0].className)
        return resolver.getZoomLevels(imageServers[0].getBaseUrl(), imageServers[0].getStorage().getBasePath() + getPath(), width, height)
    }
}