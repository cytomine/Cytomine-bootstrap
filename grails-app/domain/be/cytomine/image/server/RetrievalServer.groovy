package be.cytomine.image.server

import groovy.xml.MarkupBuilder
import be.cytomine.SequenceDomain

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 27/01/11
 * Time: 15:21
 */
class RetrievalServer extends SequenceDomain {

  String description
  String url
  int port

  def search(String pathReq, int maxSimilarPictures) {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    //xml.SEARCHPICTURE(k:maxSimilarPictures,path:image.getThumbURL())

    //String pathReq = "http://139.165.108.28:8008/images/neohisto100000/study_NEO4-grp_Curcu_INH-NEO_4_Curcu_INH_1.40_3_5_01.tif-tile_5914.png"
    //String pathReq = image.getThumbURL()
    //String pathReq = "http://is3.cytomine.be:38/adore-djatoka/resolver?url_ver=Z39.88-2004&rft_id=file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-5.jp2&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format=image/jpeg&svc.level=2&svc.rotate=0&svc.region=0,0,244,333"

    xml.SEARCHPICTURE(k:maxSimilarPictures,path:pathReq)
    String req = writer.toString()
    println "***Connect socket..."
    //Socket s = new Socket("139.165.108.28", 1230)
    Socket s = new Socket(url, port)
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
