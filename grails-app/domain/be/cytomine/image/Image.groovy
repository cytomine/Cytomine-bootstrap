package be.cytomine.image

import be.cytomine.image.acquisition.Scanner
import be.cytomine.image.server.resolvers.Resolver
import be.cytomine.image.server.ImageServer
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.vividsolutions.jts.geom.Geometry

import be.cytomine.security.User
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.SequenceDomain
import be.cytomine.rest.UrlApi
import be.cytomine.project.Slide
import be.cytomine.ontology.Annotation

class Image extends SequenceDomain {

  String filename

  Scanner scanner
  Slide slide

  String path
  Mime mime

  Integer width
  Integer height

  Double scale
  Geometry roi

  User user

  /*
   * If you modify/add an attribute, don't forget to:
   * -Update getXXXXFromData
   * -Update registerMarshaller
   * -Update functionnal test (add/edit test)
   */

  static belongsTo = Slide
  static hasMany = [ annotations : Annotation ]

  static transients = ["zoomLevels"]

  static constraints = {
    filename(blank : false)

    scanner(nullable : true)
    slide(nullable : true)

    path(nullable:false)
    mime(nullable:false)

    width(nullable:true)
    height(nullable:true)
    scale(nullable:true)
    roi(nullable:true)

    user(nullable:true)


  }

  String toString() {
    filename
  }

  def terms() {
    def terms = []
    annotations.each { annotation ->
       annotation.terms().each { term ->
          terms << term
       }
    }
    terms
  }

  static Image createImageFromData(jsonImage) {
    def image = new Image()
    getImageFromData(image,jsonImage)
  }

  static Image getImageFromData(image,jsonImage) {
    println "getImageFromData:"+ jsonImage
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

    String userId = jsonImage.user.toString()
    if(!userId.equals("null")) {
      image.user = User.get(userId)
      if(image.user==null) throw new IllegalArgumentException("User was not found with id:"+userId)
    }
    else image.user = null

    return image;
  }

  def getTermsURL() {
    return ConfigurationHolder.config.grails.serverURL + '/api/annotation/'+ this.id +'/term.json';
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Image.class
    JSON.registerObjectMarshaller(Image) {
      def returnArray = [:]
      returnArray['class'] = it.class
      println "id"
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

      returnArray['user'] = it.user? it.user.id : null
      returnArray['created'] = it.created? it.created.time.toString() : null
      returnArray['updated'] = it.updated? it.updated.time.toString() : null

      //returnArray['annotations'] = it.annotations
      //returnArray['thumb'] = it.getThumbURL()
      returnArray['thumb'] = UrlApi.getThumbURLWithImageId(it.id)
      returnArray['metadataUrl'] = UrlApi.getMetadataURLWithImageId(it.id)
      //returnArray['browse'] = ConfigurationHolder.config.grails.serverURL + "/image/browse/" + it.id

      returnArray['imageServerBaseURL'] = it.getMime().imageServers().collect { it.getBaseUrl() }
      return returnArray
    }
  }

  def getThumbURL()  {
    Collection<ImageServer> imageServers = getMime().imageServers()
    log.debug "ImageServers="+imageServers
    def urls = []
    imageServers.each {
      Resolver resolver = Resolver.getResolver(it.className)
      String url = resolver.getThumbUrl(it.getBaseUrl(), getPath())
      urls << url
    }
    if(urls.size()<1) return null //to do, send an url to a default blank image or error image

    def index = (Integer) Math.round(Math.random()*(urls.size()-1)) //select an url randomly
    log.debug "index="+index
    return urls[index]
  }

  def getMetadataURL()  {
    Set<ImageServer> imageServers = getMime().imageServers()
    def urls = []
    imageServers.each {
      Resolver resolver = Resolver.getResolver(it.className)
      String url = resolver.getMetaDataURL(it.getBaseUrl(), getPath())
      urls << url
    }
    def index = (Integer) Math.round(Math.random()*(urls.size()-1)) //select an url randomly
    return urls[index]
  }


  def getCropURL(int topLeftX, int topLeftY, int width, int height, int zoom)  {
    int deltaZoom = Math.pow(2, (getZoomLevels().max - zoom))
    Collection<ImageServer> imageServers = getMime().imageServers()
    def urls = []
    imageServers.each {
      Resolver resolver = Resolver.getResolver(it.className)
      String url = resolver.getCropURL(it.getBaseUrl(), getPath(),topLeftX,topLeftY, (int) (width / deltaZoom), (int) (height / deltaZoom),zoom)
      urls << url
    }
    def index = (Integer) Math.round(Math.random()*(urls.size()-1)) //select an url randomly
    return urls[index]
  }

  def getZoomLevels () {
    def metadata = JSON.parse(new URL(getMetadataURL()).text)
    int max = Integer.parseInt(metadata.levels)
    int min = 0
    int middle = ((max - min) / 2)
    return [min : 0, max : max, middle : middle]
  }



}