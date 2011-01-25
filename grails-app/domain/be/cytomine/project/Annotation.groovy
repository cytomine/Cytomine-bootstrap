package be.cytomine.project
import com.vividsolutions.jts.geom.MultiPolygon

class Annotation {

   String name
   MultiPolygon location
   Scan scan

    static belongsTo = [scan:Scan]

    static constraints = {
    }

    static mapping = {
      columns {
        location type: org.hibernatespatial.GeometryUserType
      }
    }


}
