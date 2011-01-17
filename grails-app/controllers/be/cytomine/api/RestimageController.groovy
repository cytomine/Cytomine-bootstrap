package be.cytomine.api

import be.cytomine.project.Scan
import be.cytomine.server.ImageServer
import be.cytomine.server.resolvers.*



class RestimageController {

  def thumb = {
    Scan scan = Scan.findById(params.idscan)
    def out = new ByteArrayOutputStream()
    out << new URL(scan.getThumbURL()).openStream()
    response.contentLength = out.size();
    if (request.method == 'HEAD') { render(text: "", contentType: "image/jpeg"); }
    else {
      response.contentType = "image/jpeg"; response.outputStream << out.toByteArray()
    }
  }


  def metadata = {
    Scan scan = Scan.findById(params.idscan)
    def url = new URL(scan.getMetadataURL())
    render(contentType: "application/json", text: "${url.text}")
  }


  def crop = {
    Scan scan = Scan.findById(params.idscan)
    println params.topleftx
    int topLeftX = Integer.parseInt(params.topleftx)
    int topLeftY = Integer.parseInt(params.toplefty)
    int width = Integer.parseInt(params.width)
    int height = Integer.parseInt(params.height)
    int zoom = Integer.parseInt(params.zoom)

    def out = new ByteArrayOutputStream()
    out << new URL(scan.getCropURL(topLeftX, topLeftY, width, height, zoom)).openStream()

    response.contentLength = out.size()
    if (request.method == 'HEAD') {
      render(text: "", contentType: "image/jpeg");
    }
    else {
      response.contentType = "image/jpeg"; response.outputStream << out.toByteArray()
    }
  }


}



