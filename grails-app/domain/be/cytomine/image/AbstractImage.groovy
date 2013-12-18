package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.acquisition.Instrument
import be.cytomine.image.server.ImageServerStorage
import be.cytomine.image.server.MimeImageServer
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.laboratory.Sample
import be.cytomine.security.SecUser
import be.cytomine.server.resolvers.Resolver
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * An abstract image is an image that can be map with projects.
 * When an "AbstractImage" is add to a project, a "ImageInstance" is created.
 */
class AbstractImage extends CytomineDomain implements Serializable {

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
    SecUser user //owner

    static belongsTo = Sample

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
        user(nullable: true)
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


    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     * @throws CytomineException Error during properties copy (wrong argument,...)
     */
    static AbstractImage insertDataIntoDomain(def json,def domain = new AbstractImage()) throws CytomineException {
        println "insertDataIntoDomain="+json
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.filename = JSONUtils.getJSONAttrStr(json,'filename')
        domain.path = JSONUtils.getJSONAttrStr(json,'path')
        domain.height = JSONUtils.getJSONAttrInteger(json,'height',-1)
        domain.width = JSONUtils.getJSONAttrInteger(json,'width',-1)
        domain.created = JSONUtils.getJSONAttrDate(json,'created')
        domain.updated = JSONUtils.getJSONAttrDate(json,'updated')
        domain.scanner = JSONUtils.getJSONAttrDomain(json,"scanner",new Instrument(),false)
        domain.sample = JSONUtils.getJSONAttrDomain(json,"sample",new Sample(),false)
        domain.mime = JSONUtils.getJSONAttrDomain(json,"mime",new Mime(),'extension','String',true)

        if (domain.mime.imageServers().size() == 0) {
            throw new WrongArgumentException("Mime with id:${json.mime} has not image server")
        }
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {

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
            returnArray['mime'] = it.mime?.extension
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
            returnArray['width'] = it.width
            returnArray['height'] = it.height
            returnArray['depth'] = it.getZoomLevels()?.max
            returnArray['resolution'] = it.resolution
            returnArray['magnification'] = it.magnification
            returnArray['thumb'] = it.getThumbURL()
            returnArray['metadataUrl'] = UrlApi.getMetadataURLWithImageId(it.id)
            return returnArray
        }
    }

    def getImageServersStorage() {
        try {
            def imageServers = MimeImageServer.findAllByMime(this.getMime())?.collect {it.imageServer}.findAll{it.available}
            def storageAbstractImage = StorageAbstractImage.findAllByAbstractImage(this)?.collect { it.storage }
            if (imageServers.isEmpty() || storageAbstractImage.isEmpty()) return []
            else {
                return ImageServerStorage.createCriteria().list {
                    inList("imageServer",  imageServers)
                    inList("storage", storageAbstractImage )
                }
            }
        } catch (Exception e) {
            //may appear during tests
            //this method does not work with an unsaved domain or a domain instance with transients values
            //find another way to handle the error ?
            log.error "cannot get imageServerStorage from AbstractImage $this"
            return null
        }



    }

    def getPreviewURL() {
        if (this.width != null && this.height != null)
            return getCropURLWithMaxWithOrHeight(0, this.height, this.width, this.height, 512, 512)
        else
            return null
    }

    def getThumbURL() {
        if (this.width != null && this.height != null)
            return getCropURLWithMaxWithOrHeight(0, this.height, this.width, this.height, 256, 256)
        else
            return null
    }

    def getMetadataURL() {
        def imageServerStorages = getImageServersStorage()
        if (imageServerStorages == null || imageServerStorages.size() == 0) {
            null
        }
        def index = (Integer) Math.round(Math.random() * (imageServerStorages.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServerStorages[index].imageServer.className)
        if (!resolver) return null
        Storage storage = StorageAbstractImage.findAllByAbstractImage(this).first().storage
        String url = resolver.getMetaDataURL(imageServerStorages[index].imageServer.getBaseUrl(), [storage.getBasePath(), getPath()].join(File.separator))
        return url
    }

    def getCropURLWithMaxWithOrHeight(int topLeftX, int topLeftY, int width, int height, int desiredWidth, int desiredHeight) {
        def imageServerStorages = getImageServersStorage()

        if (imageServerStorages == null || imageServerStorages.size() == 0) {
            return null
        }
        def index = (Integer) Math.round(Math.random() * (imageServerStorages.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServerStorages[index].imageServer.className)
        if (!resolver) return null
        def baseUrl = imageServerStorages[index].imageServer.getBaseUrl()
        Storage storage = StorageAbstractImage.findAllByAbstractImage(this).first().storage

        String basePath = storage.getBasePath()
        String path = getPath()
        int widthImg =  this.getWidth()
        int heightImg = this.getHeight()
        resolver.getCropURL(baseUrl, [basePath, path].join(File.separator), topLeftX, topLeftY, width, height, widthImg, heightImg, desiredWidth, desiredHeight)
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
        def imageServerStorages = getImageServersStorage()
        if (imageServerStorages == null || imageServerStorages.size() == 0 || width == null || height == null) return null
        Resolver resolver = Resolver.getResolver(imageServerStorages[0].imageServer.className)
        if (!resolver) return null
        Storage storage = StorageAbstractImage.findAllByAbstractImage(this).first().storage
        return resolver?.getZoomLevels(imageServerStorages[0].imageServer.getBaseUrl(), [storage.getBasePath(), getPath()].join(File.separator), width, height)
    }
}