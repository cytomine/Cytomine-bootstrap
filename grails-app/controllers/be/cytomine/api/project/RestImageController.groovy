package be.cytomine.api.project

import be.cytomine.project.Scan
import groovy.xml.MarkupBuilder
import grails.converters.*
import be.cytomine.project.Annotation
import com.vividsolutions.jts.geom.Coordinate
import be.cytomine.server.RetrievalServer

class RestImageController {

  def thumb = {
    Scan scan = Scan.findById(params.idscan)
    def out = new ByteArrayOutputStream()
    out << new URL(scan.getThumbURL()).openStream()
    response.contentLength = out.size();
    if (request.method == 'HEAD') { render(text: "", contentType: "image/jpeg"); }
    else {
      response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
    }
  }


  def metadata = {
    Scan scan = Scan.findById(params.idscan)
    def url = new URL(scan.getMetadataURL())
    render(contentType: "application/json", text: "${url.text}")
  }

  def crop = {
    Annotation annotation = Annotation.findById(params.idannotation)
    int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getScan().getZoomLevels().middle

    if (annotation == null || zoom < annotation.getScan().getZoomLevels().min || zoom > annotation.getScan().getZoomLevels().max) {
      response.status = 404
      render "404"
      return
    }

    def out = new ByteArrayOutputStream()
    out << new URL(annotation.getCropURL(zoom)).openStream()

    response.contentLength = out.size()
    if (request.method == 'HEAD') {
      render(text: "", contentType: "image/jpeg");
    }
    else {
      response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
    }
  }

  def retrieval = {
    Annotation annotation = Annotation.findById(params.idannotation)
    int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getScan().getZoomLevels().middle
    int maxSimilarPictures = Integer.parseInt(params.maxsimilarpictures)
    def retrievalServers = RetrievalServer.findAll()
    println annotation.getCropURL(1)
    def list = retrievalServers.get(0).search(annotation.getCropURL(zoom), maxSimilarPictures)

    withFormat {
      json { render list as JSON }
      xml { render list as XML}
    }
  }



}



