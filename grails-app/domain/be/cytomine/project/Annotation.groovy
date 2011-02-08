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
  static hasMany = [ annotationTerm: AnnotationTerm ]

  static transients = ["cropURL", "boundaries"]

  static constraints = {
  }

  static mapping = {
    id (generator:'assigned', unique : true)
    columns {
      location type: org.hibernatespatial.GeometryUserType
    }
  }
  /* Get all terms map with the annotation */
  def terms() {
    return annotationTerm.collect{it.term}
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

  boolean equals(object) {
       if(object instanceof Annotation)
       {
          (this.id==object.id && this.name.equals(object.name) && this.location==object.location && this.scan.id==object.scan.id)
       }
       else false
  }

  static def convertToMap(Annotation annotation){
      HashMap jsonMap = new HashMap()

      jsonMap.annotation = [id: annotation.id, name: annotation.name, location: annotation.location.toString(), scan: annotation.scan.id,'class':annotation.class]
      if (annotation.id==null)  jsonMap.annotation.remove('id')
      jsonMap
  }

  static Annotation createOrGetBasicAnnotation() {
    println "createOrGetBasicAnnotation()"
    def annotation = new Annotation(location:new WKTReader().read("POINT(17573.5 21853.5)"), name:"test",scan:Scan.createOrGetBasicScan())

    println "annotation.validate()=" + annotation.validate()
    annotation.save(flush : true)
    annotation
  }
  
  
  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Annotation.class
    JSON.registerObjectMarshaller(Annotation) {
      def returnArray = [:]
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['location'] = it.location.toString()
      returnArray['scan'] = it.scan.id
      return returnArray
    }
  }
}
