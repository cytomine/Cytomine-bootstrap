package be.cytomine.api.project

import be.cytomine.project.Image

import grails.converters.*
import be.cytomine.project.Annotation

import be.cytomine.server.RetrievalServer

class RestImageController {

  def thumb = {
    Image scan = Image.findById(params.id)
    print scan.getThumbURL()
    def out = new ByteArrayOutputStream()
    out << new URL(scan.getThumbURL()).openStream()
    response.contentLength = out.size();
    withFormat {
      jpg {
        if (request.method == 'HEAD') {
          render(text: "", contentType: "image/jpeg");
        }
        else {
          response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
        }
      }
    }
  }


  def metadata = {
    Image scan = Image.findById(params.id)
    def url = new URL(scan.getMetadataURL())
    withFormat {
      json {
        render(contentType: "application/json", text: "${url.text}")
      }
    }

  }

  def crop = {
    Annotation annotation = Annotation.findById(params.id)
    int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getImage().getZoomLevels().middle

    if (annotation == null || zoom < annotation.getImage().getZoomLevels().min || zoom > annotation.getImage().getZoomLevels().max) {
      response.status = 404
      render "404"
      return
    }

    def out = new ByteArrayOutputStream()
    out << new URL(annotation.getCropURL(zoom)).openStream()

    response.contentLength = out.size()

    withFormat {
      jpg {
        if (request.method == 'HEAD') {
          render(text: "", contentType: "image/jpeg");
        }
        else {
          response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
        }
      }
    }
  }

  def retrieval = {
    Annotation annotation = Annotation.findById(params.idannotation)
    int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getImage().getZoomLevels().middle
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



