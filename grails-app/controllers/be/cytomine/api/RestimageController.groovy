package be.cytomine.api

import be.cytomine.project.Scan
import groovy.xml.MarkupBuilder

class RestimageController {

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
    Scan scan = Scan.findById(params.idscan)

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
      response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
    }
  }


  def retrieval = {
    //Sample code to call Pixit-Retrival. Must be more generic (use with crop,...)
    println "retrieval"
    Scan scan = Scan.findById(params.idscan)
    println "scan: " + params.idscan

    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.SEARCHPICTURE(k:10,path:scan.data.path)

    println "xml=" + writer.toString()

    //TODO: send to socket au server:1234

    //TODO: get xml similar list from socket

    //TODO: decode this list

    //TODO: print pictures

  }


}



