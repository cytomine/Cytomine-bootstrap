package be.cytomine.image

import be.cytomine.image.acquisition.Scanner

import be.cytomine.image.server.ImageServer
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.SequenceDomain
import be.cytomine.rest.UrlApi
import be.cytomine.project.Slide
import be.cytomine.server.resolvers.Resolver
import be.cytomine.image.server.StorageAbstractImage

class AbstractImage extends SequenceDomain {

    String filename
    Scanner scanner
    Slide slide
    String path
    Mime mime
    Integer width
    Integer height
    Double scale
    Geometry roi

    /*
    * If you modify/add an attribute, don't forget to:
    * -Update getXXXXFromData
    * -Update registerMarshaller
    * -Update functionnal test (add/edit test)
    */

    static belongsTo = Slide

    static hasMany = [ abstractimagegroup : AbstractImageGroup, storageAbstractImages : StorageAbstractImage ]

    static transients = ["zoomLevels"]

    static constraints = {
        filename(blank : false, unique : true)

        scanner(nullable : true)
        slide(nullable : true)

        path(nullable:false)
        mime(nullable:false)

        width(nullable:true)
        height(nullable:true)
        scale(nullable:true)
        roi(nullable:true)

    }

    String toString() {
        filename
    }

    def groups() {
        return abstractimagegroup.collect{
            it.group
        }
    }


    static AbstractImage createFromData(jsonImage) {
        def image = new AbstractImage()
        getFromData(image,jsonImage)
    }

    static AbstractImage getFromData(image,jsonImage) {
        println "getFromData:"+ jsonImage
        image.filename = jsonImage.filename
        image.path = jsonImage.path

        image.height = (!jsonImage.height.toString().equals("null"))  ? ((String)jsonImage.height).toInteger() : -1
        image.width = (!jsonImage.width.toString().equals("null"))  ? ((String)jsonImage.width).toInteger() : -1
        image.scale = (!jsonImage.scale.toString().equals("null"))  ? ((String)jsonImage.scale).toDouble() : -1

        image.created = (!jsonImage.created.toString().equals("null"))  ? new Date(Long.parseLong(jsonImage.created)) : null
        image.updated = (!jsonImage.updated.toString().equals("null"))  ? new Date(Long.parseLong(jsonImage.updated)) : null

        String scannerId = jsonImage.scanner.toString()
        if(!scannerId.equals("null")) {
            image.scanner = Scanner.get(scannerId)
            if (image.scanner==null) throw new IllegalArgumentException("Scanner was not found with id:"+ scannerId)
        }
        else image.scanner = null

        String slideId = jsonImage.slide.toString()
        if(!slideId.equals("null")) {
            image.slide = Slide.get(slideId)
            if(image.slide==null) throw new IllegalArgumentException("Slide was not found with id:"+ slideId)
        }
        else image.slide = null

        String mimeId = jsonImage.mime.toString()
        image.mime = Mime.findByExtension(mimeId)
        if(image.mime==null) {
            throw new IllegalArgumentException("Mime was not found with id:"+ mimeId)
        }
        else if(image.mime.imageServers().size()==0) {
            throw new IllegalArgumentException("Mime with id:"+ mimeId + " has not image server")
        }

        String roi = jsonImage.roi.toString()
        if(!roi.equals("null"))
        {
            try { image.roi = new WKTReader().read(roi)}
            catch(com.vividsolutions.jts.io.ParseException e)
            {
                throw new IllegalArgumentException("Bad Geometry:"+ e.getMessage())
            }

        }
        else image.roi = null

        return image;
    }

    def getTermsURL() {
        return ConfigurationHolder.config.grails.serverURL + '/api/annotation/'+ this.id +'/term.json';
    }


    static void registerMarshaller() {

        println "Register custom JSON renderer for " + AbstractImage.class
        JSON.registerObjectMarshaller(AbstractImage) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['filename'] = it.filename
            returnArray['scanner'] = it.scanner? it.scanner.id : null
            returnArray['slide'] = it.slide? it.slide.id : null
            returnArray['path'] = it.path
            returnArray['mime'] = it.mime.extension
            returnArray['width'] = it.width
            returnArray['height'] = it.height
            returnArray['scale'] = it.scale
            returnArray['roi'] = it.roi.toString()
            returnArray['created'] = it.created? it.created.time.toString() : null
            returnArray['updated'] = it.updated? it.updated.time.toString() : null
            //returnArray['annotations'] = it.annotations
            /*returnArray['thumb'] = it.getThumbURL()*/
            returnArray['thumb'] = it.getThumbURL()
            returnArray['metadataUrl'] = UrlApi.getMetadataURLWithImageId(it.id)
            returnArray['imageServerInfos'] = UrlApi.getImageServerInfosWithImageId(it.id)
            //returnArray['browse'] = ConfigurationHolder.config.grails.serverURL + "/image/browse/" + it.id
            returnArray['imageServerBaseURL'] = it.getMime().imageServers().collect { it.getBaseUrl() }
            return returnArray
        }
    }


    def getPreviewURL()  {
        Collection<ImageServer> imageServers = getMime().imageServers()
        def urls = []
        imageServers.each {
            Resolver resolver = Resolver.getResolver(it.className)
            String url = resolver.getPreviewUrl(it.getBaseUrl(), getPath())
            urls << url
        }
        if(urls.size()<1) return null //to do, send an url to a default blank image or error image

        def index = (Integer) Math.round(Math.random()*(urls.size()-1)) //select an url randomly

        return urls[index]
    }

    def getThumbURL()  {
        Collection<ImageServer> imageServers = getMime().imageServers()
        if (imageServers.size() == 0) {
            return "images/cytomine.jpg"
        }
        def index = (Integer) Math.round(Math.random()*(imageServers.size()-1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getThumbUrl(imageServers[index].getBaseUrl(), getPath())
        return url
    }

    def getMetadataURL()  {
        Collection<ImageServer> imageServers = getMime().imageServers()
        if (imageServers.size() == 0) {
            return [] as JSON
        }
        def index = (Integer) Math.round(Math.random()*(imageServers.size()-1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getMetaDataURL(imageServers[index].getBaseUrl(), getPath())
        return url
    }

    def getPropertiesURL()  {
        Collection<ImageServer> imageServers = getMime().imageServers()
        if (imageServers.size() == 0) {
            return [] as JSON
        }
        def index = (Integer) Math.round(Math.random()*(imageServers.size()-1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getPropertiesURL(imageServers[index].getBaseUrl(), getPath())
        return url
    }

    def getCropURL(int topLeftX, int topLeftY, int width, int height)  {
        Collection<ImageServer> imageServers = getMime().imageServers()
        if (imageServers.size() == 0) {
            return "images/cytomine.jpg"
        }
        def index = (Integer) Math.round(Math.random()*(imageServers.size()-1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getCropURL(imageServers[index].getBaseUrl(), getPath(),topLeftX,topLeftY, width, height)
        return url
    }

    def getCropURL(int topLeftX, int topLeftY, int width, int height, int zoom)  {
        Collection<ImageServer> imageServers = getMime().imageServers()
        if (imageServers.size() == 0) {
            return "images/cytomine.jpg"
        }
        def index = (Integer) Math.round(Math.random()*(imageServers.size()-1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServers[index].className)
        String url = resolver.getCropURL(imageServers[index].getBaseUrl(), getPath(),topLeftX,topLeftY, width, height ,zoom)
        return url
    }

    def getZoomLevels () {
        Collection<ImageServer> imageServers = getMime().imageServers()
        assert(imageServers.size() > 0)
        Resolver resolver = Resolver.getResolver(imageServers[0].className)
        return resolver.getZoomLevels(imageServers[0].getBaseUrl(), getPath())
    }



}