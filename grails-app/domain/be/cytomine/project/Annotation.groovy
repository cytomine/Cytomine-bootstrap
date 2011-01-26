package be.cytomine.project
import com.vividsolutions.jts.geom.Geometry

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





}
