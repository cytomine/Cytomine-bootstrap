package be.cytomine.api.project

import be.cytomine.project.Scan
import groovy.xml.MarkupBuilder
import grails.converters.*

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
    Scan scan = Scan.findById(params.idscan)
    int maxSimilarPictures = Integer.parseInt(params.maxsimilarpictures)
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    //xml.SEARCHPICTURE(k:maxSimilarPictures,path:scan.getThumbURL())
     xml.SEARCHPICTURE(k:maxSimilarPictures,path:"/var/www/images/neohisto/study_NEO13-grp_CNS-NEO13_CNS_2.10_4_3_01.tif-tile_9567.png")
    String req = writer.toString()
    println "***Connect socket..."
    Socket s = new Socket("139.165.108.28", 1230)
    println "***Write request on socket..." + req
    s << req +"\n"
    s << "STOP\n"
    String xmlString =""
    println "***Read response from socket..."
    s.withStreams { inStream, outStream ->
      def reader = inStream.newReader()

      String line = ""
      while (!line.equals("STOP"))
      {
        println line
        line = reader.readLine()
        if(!line.equals("STOP")) xmlString = xmlString + line;
      }
    }

    println "***Read: "+  xmlString;

    def xmlObj = new XmlParser().parseText(xmlString)
    def list = []
    xmlObj.pict.each {
      list << [path:it.attribute("id"),sim:it.attribute("sim")]
    }
    render list as JSON
  }

}



