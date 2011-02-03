package be.cytomine.project
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Coordinate
import grails.converters.*
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Geometry


class Annotation {

  String name
  Geometry location
  Scan scan

  static belongsTo = [scan:Scan]

  static transients = ["cropURL", "boundaries"]

  static constraints = {
  }

  static mapping = {
    id (generator:'assigned', unique : true)
    columns {
      location type: org.hibernatespatial.GeometryUserType
    }
  }

  def beforeInsert() {
    if (id == null)
      id = Annotation.generateID()
  }

  private def getBoundaries () {
    def metadata = JSON.parse(new URL(scan.getMetadataURL()).text)
    Coordinate[] coordinates = location.getEnvelope().getCoordinates()

    int topLeftX = coordinates[3].x
    int topLeftY = Integer.parseInt(metadata.height) - coordinates[3].y
    int width =  coordinates[1].x - coordinates[0].x
    int height =  coordinates[3].y - coordinates[0].y
    int zoom = Integer.parseInt(metadata.levels)
    return [topLeftX : topLeftX, topLeftY : topLeftY,width : width, height : height, zoom : zoom]
  }

  def getCropURL(int zoom) {
    def boundaries = getBoundaries()
    return scan.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, zoom)
  }

  static Annotation getAnnotationFromData(data) {
    def annotation = new Annotation()
    annotation.name = data.annotation.name
    annotation.location = new WKTReader().read(data.annotation.location);
    annotation.scan = Scan.get(data.annotation.scan);
    return annotation;
  }


  static int generateID() {
    int max = 0
    Annotation.list().each { annot ->
      max = Math.max(max, annot.id)
    }
    return ++max
  }

  static def convertToMap(Annotation annotation){
      HashMap jsonMap = new HashMap()
      jsonMap.annotation = [id: annotation.id, name: annotation.name, location: annotation.location.toString(), scan: annotation.scan.id]
      jsonMap
  }
}
