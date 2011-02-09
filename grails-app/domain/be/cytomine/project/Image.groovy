package be.cytomine.project

import be.cytomine.warehouse.Data
import be.cytomine.acquisition.Scanner
import be.cytomine.server.resolvers.Resolver
import be.cytomine.server.ImageServer
import grails.converters.JSON
import be.cytomine.test.BasicInstance

class Image {
  String filename
  Data data
  Scanner scanner
  Slide slide

  static belongsTo = Slide
  static hasMany = [ annotations : Annotation ]

  static transients = ["zoomLevels"]

  static constraints = {
    filename (blank : false)
    data (blank : false)
    scanner (blank : false , nullable : true)
    slide nullable : true
  }

  String toString() {
    filename + " (" + slide.getName() + ")"
  }

  def getThumbURL()  {
    Collection<ImageServer> imageServers = getData().getMime().imageServers()
    def urls = []
    imageServers.each {
      Resolver resolver = Resolver.getResolver(it.className)
      String url = resolver.getThumbUrl(it.getBaseUrl(), getData().getPath())
      urls << url
    }
    def index = (Integer) Math.round(Math.random()*(urls.size()-1)) //select an url randomly
    return urls[index]
  }

  def getMetadataURL()  {
    Set<ImageServer> imageServers = getData().getMime().imageServers()
    def urls = []
    imageServers.each {
      Resolver resolver = Resolver.getResolver(it.className)
      String url = resolver.getMetaDataURL(it.getBaseUrl(), getData().getPath())
      urls << url
    }
    def index = (Integer) Math.round(Math.random()*(urls.size()-1)) //select an url randomly
    return urls[index]
  }


  def getCropURL(int topLeftX, int topLeftY, int width, int height, int zoom)  {
    int deltaZoom = Math.pow(2, (getZoomLevels().max - zoom))
    Collection<ImageServer> imageServers = getData().getMime().imageServers()
    def urls = []
    imageServers.each {
      Resolver resolver = Resolver.getResolver(it.className)
      String url = resolver.getCropURL(it.getBaseUrl(), getData().getPath(),topLeftX,topLeftY, (int) (width / deltaZoom), (int) (height / deltaZoom),zoom)
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
