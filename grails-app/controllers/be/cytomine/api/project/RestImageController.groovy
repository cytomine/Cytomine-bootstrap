package be.cytomine.api.project

import be.cytomine.project.Scan
import groovy.xml.MarkupBuilder
import grails.converters.*
import be.cytomine.project.Annotation
import com.vividsolutions.jts.geom.Coordinate

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
    Annotation annotation = Annotation.findById(params.idannotation)

    if (scan == null || annotation == null) {
      response.status = 404
      render "404"
      return
    }

    def out = new ByteArrayOutputStream()
    out << new URL(annotation.getCropURL()).openStream()

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
    int maxSimilarPictures = Integer.parseInt(params.maxsimilarpictures)

    def list = retrieval(annotation.getCropURL(), maxSimilarPictures)

    withFormat {
      json { render list as JSON }
      xml { render list as XML}
    }
  }

  /*def retrievalscan = {
    Scan scan = Scan.findById(params.idscan)
    int maxSimilarPictures = Integer.parseInt(params.maxsimilarpictures)

    def list = retrieval(scan.getThumbURL(), maxSimilarPictures)

    withFormat {
      json { render list as JSON }
      xml { render list as XML}
    }
  }*/

  private def retrieval (String pathReq, int maxSimilarPictures) {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    //xml.SEARCHPICTURE(k:maxSimilarPictures,path:scan.getThumbURL())

    //String pathReq = "http://139.165.108.28:8008/images/neohisto100000/study_NEO4-grp_Curcu_INH-NEO_4_Curcu_INH_1.40_3_5_01.tif-tile_5914.png"
    //String pathReq = scan.getThumbURL()
    //String pathReq = "http://is3.cytomine.be:38/adore-djatoka/resolver?url_ver=Z39.88-2004&rft_id=file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-5.jp2&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format=image/jpeg&svc.level=2&svc.rotate=0&svc.region=0,0,244,333"

    xml.SEARCHPICTURE(k:maxSimilarPictures,path:pathReq)
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
    list << [path:pathReq,sim:"1"]
    xmlObj.pict.each {
      list << [path:it.attribute("id"),sim:it.attribute("sim")]
    }

    return list
  }

}



