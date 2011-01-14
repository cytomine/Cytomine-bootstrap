package be.cytomine.project

import be.cytomine.warehouse.Data
import be.cytomine.acquisition.Scanner
import be.cytomine.server.resolvers.Resolver
import be.cytomine.server.ImageServer
import java.net.URL

class Scan {
  String filename
  Data data
  Scanner scanner

  static constraints = {
    filename blank : false
    data blank : false
    scanner blank : false
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
}
