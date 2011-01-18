package be.cytomine.project

import be.cytomine.warehouse.Data
import be.cytomine.acquisition.Scanner
import be.cytomine.server.resolvers.Resolver
import be.cytomine.server.ImageServer

class Scan {
  String filename
  Data data
  Scanner scanner
  Slide slide

  static belongsTo = Slide

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
    Collection<ImageServer> imageServers = getData().getMime().imageServers()
    def urls = []
    imageServers.each {
      Resolver resolver = Resolver.getResolver(it.className)
      String url = resolver.getCropURL(it.getBaseUrl(), getData().getPath(),topLeftX,topLeftY,width,height,zoom)
      urls << url
    }
    def index = (Integer) Math.round(Math.random()*(urls.size()-1)) //select an url randomly
    return urls[index]
  }
}
