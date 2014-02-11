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
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * An abstract image is an image that can be map with projects.
 * When an "AbstractImage" is add to a project, a "ImageInstance" is created.
 */
@ApiObject(name = "abstract image", description = "A real image store on disk, see 'image instance' for an image link in a project")
class AbstractImage extends CytomineDomain implements Serializable {

    @ApiObjectFieldLight(description = "The image short filename (will be show in GUI)", useForCreation = false)
    String originalFilename

    @ApiObjectFieldLight(description = "The exact image full filename")
    String filename

    @ApiObjectFieldLight(description = "The instrument that digitalize the image", mandatory = false)
    Instrument scanner

    @ApiObjectFieldLight(description = "The source of the image (human, annimal,...)", mandatory = false)
    Sample sample

    @ApiObjectFieldLight(description = "The full image path directory")
    String path

    @ApiObjectFieldLight(description = "The image type. For creation, use the ext (not the mime id!)")
    Mime mime

    @ApiObjectFieldLight(description = "The image width lenght", mandatory = false, defaultValue = "-1")
    Integer width

    @ApiObjectFieldLight(description = "The image height lenght", mandatory = false, defaultValue = "-1")
    Integer height

    @ApiObjectFieldLight(description = "The image max zoom")
    Integer magnification

    @ApiObjectFieldLight(description = "The image resolution (microm per pixel)")
    Double resolution

    @ApiObjectFieldLight(description = "The image owner", mandatory = false, defaultValue = "current user")
    SecUser user //owner

    static belongsTo = Sample

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "metadataUrl", description = "URL to get image file metadata",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "thumb", description = "URL to get abstract image short view (htumb)",allowedType = "string",useForCreation = false)
    ])
    static transients = ["zoomLevels", "thumbURL", MIME_WITH_MACRO_IMAGES]

    private static MIME_WITH_MACRO_IMAGES = ["scn", "mrxs", "ndpi", "vms", "svs"]

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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def image) {
        def returnArray = CytomineDomain.getDataFromDomain(image)
        returnArray['filename'] = image?.filename
        returnArray['originalFilename'] = image?.originalFilename
        returnArray['scanner'] = image?.scanner?.id
        returnArray['sample'] = image?.sample?.id
        returnArray['path'] = image?.path
        returnArray['mime'] = image?.mime?.extension
        returnArray['width'] = image?.width
        returnArray['height'] = image?.height
        returnArray['depth'] = image?.getZoomLevels()?.max
        returnArray['resolution'] = image?.resolution
        returnArray['magnification'] = image?.magnification
        returnArray['thumb'] = image?.getThumbURL()
        returnArray['fullPath'] = image?.getFullPath()
        returnArray['macroURL'] = image?.getMacroURL()
        returnArray['metadataUrl'] = UrlApi.getMetadataURLWithImageId(image?.id)
        returnArray
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
        if (this.width != null && this.height != null)   {
            def boundaries = [:]
            boundaries.topLeftX = 0
            boundaries.topLeftY = this.height
            boundaries.width = this.width
            boundaries.height = this.height
            boundaries.scale = 512
            return getCropURL(boundaries)
        } else {
            return null
        }
    }

    def getMacroURL() {
        if (MIME_WITH_MACRO_IMAGES.contains(mime.extension))
            return UrlApi.getAssociatedImage(id, "macro", 256);
        else
            return getThumbURL()
    }

    def getThumbURL() {
        if (this.width != null && this.height != null)   {
            def boundaries = [:]
            boundaries.topLeftX = 0
            boundaries.topLeftY = this.height
            boundaries.width = this.width
            boundaries.height = this.height
            boundaries.scale = 256
            return getCropURL(boundaries)
        } else {
            return null
        }
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

    def getFullPath() {
        if(this.version) {
            def imageServersStorage = getImageServersStorage()
            if (imageServersStorage && imageServersStorage.size() > 0)
                return [imageServersStorage.first().storage.getBasePath(), getPath()].join(File.separator)
            else
                return null
        }
         else {
            return null
        }
    }

    def getCropURL(def boundaries) {
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

        boundaries.baseImageWidth =this.getWidth()
        boundaries.baseImageHeight =this.getHeight()
        resolver.getCropURL(baseUrl, [basePath, path].join(File.separator), boundaries)
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