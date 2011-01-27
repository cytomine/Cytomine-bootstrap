package be.cytomine.project
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Coordinate
import grails.converters.JSON

class Annotation {

  String name
  Geometry location
  Scan scan

  static belongsTo = [scan:Scan]

  static constraints = {
  }

  static mapping = {
    columns {
      location type: org.hibernatespatial.GeometryUserType
    }
  }

  def getBoundaries () {
    def metadata = JSON.parse(new URL(scan.getMetadataURL()).text)
    Coordinate[] coordinates = location.getEnvelope().getCoordinates()

    int topLeftX = coordinates[3].x
    int topLeftY = Integer.parseInt(metadata.height) - coordinates[3].y
    int width =  coordinates[1].x - coordinates[0].x
    int height =  coordinates[3].y - coordinates[0].y
    int zoom = Integer.parseInt(metadata.levels)
    return [topLeftX : topLeftX, topLeftY : topLeftY,width : width, height : height, zoom : zoom]
  }





}
