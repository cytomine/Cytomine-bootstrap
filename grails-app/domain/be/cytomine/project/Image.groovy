package be.cytomine.project

import be.cytomine.acquisition.Scanner
import be.cytomine.server.resolvers.Resolver
import be.cytomine.server.ImageServer
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.vividsolutions.jts.geom.Geometry
import be.cytomine.warehouse.Mime
import be.cytomine.security.User
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.SequenceDomain

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

  static Image createImageFromData(jsonImage) {
    def image = new Image()
    getImageFromData(image,jsonImage)
  }
  static Image getImageFromData(image,jsonImage) {
    image.filename = jsonImage.filename
    image.scanner = Scanner.get(jsonImage.scanner)
    image.slide = !jsonImage.slide.toString().equals("null")? Slide.get(jsonImage.slide) : null
    image.path = jsonImage.path
    image.mime = !jsonImage.mime.toString().equals("null")? Mime.get(jsonImage.mime) : null
    image.height = jsonImage.height
    image.width = jsonImage.width
    image.scale = (!jsonImage.scale.toString().equals("null"))  ? ((String)jsonImage.scale).toDouble() : -1
    image.roi = !jsonImage.roi.toString().equals("null")? new WKTReader().read(jsonImage.roi) : null
    image.user =  User.get(jsonImage.user);
    image.created = (!jsonImage.created.toString().equals("null"))  ? new Date(Long.parseLong(jsonImage.created)) : null
    image.updated = (!jsonImage.updated.toString().equals("null"))  ? new Date(Long.parseLong(jsonImage.updated)) : null
    return image;
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Image.class
    JSON.registerObjectMarshaller(Image) {
      def returnArray = [:]
      returnArray['class'] = it.class
      println "id"
      returnArray['id'] = it.id
      //returnArray['annotations'] = it.annotations
      returnArray['path'] = it.path
      returnArray['mime'] = it.mime.id
      returnArray['filename'] = it.filename
      returnArray['scanner'] = it.scanner? it.scanner.id : null
      returnArray['slide'] = it.slide? it.slide.id : null
      returnArray['thumb'] = it.getThumbURL()
      returnArray['metadataUrl'] = ConfigurationHolder.config.grails.serverURL + "/api/image/"+it.id+"/metadata.json"
      returnArray['browse'] = ConfigurationHolder.config.grails.serverURL + "/image/browse/" + it.id
      returnArray['imageServerBaseURL'] = it.getMime().imageServers().url
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
      log.debug "url="+url
      urls << url
    }
    if(urls.size()<1) return null

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
