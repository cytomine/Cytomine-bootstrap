package be.cytomine.project
import com.vividsolutions.jts.geom.MultiPolygon

class Annotation {

   String name
   MultiPolygon location

  static belongsTo = Scan

    static constraints = {
    }

    static mapping = {
      columns {
        location type: org.hibernatespatial.GeometryUserType
      }
    }


}
