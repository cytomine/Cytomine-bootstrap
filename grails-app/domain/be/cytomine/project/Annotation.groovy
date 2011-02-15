package be.cytomine.project
import grails.converters.*
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.io.WKTReader


class Annotation {

  def sequenceService

  String name
  Geometry location
  Image image
  Double zoomLevel
  String channels

  Date created
  Date updated
  Date deleted

  static belongsTo = [image:Image]
  static hasMany = [ annotationTerm: AnnotationTerm ]

  static transients = ["cropURL", "boundaries"]

  static constraints = {
    name(blank:false)
    location(nullable:false)
    zoomLevel(nullable:true)
    channels(nullable:true)
    created(nullable:true)
    updated(nullable:true)
    deleted(nullable:true)
  }

  static mapping = {
    id (generator:'assigned' , unique : true)
    columns {
      location type: org.hibernatespatial.GeometryUserType
    }
  }
  /* Get all terms map with the annotation */
  def terms() {
    return annotationTerm.collect{it.term}
  }

   def beforeInsert() {
    created = new Date()
    if (id == null)
      id = sequenceService.generateID(this)
   }

   def beforeUpdate() {
       updated = new Date()
   }

  private def getBoundaries () {
    def metadata = JSON.parse(new URL(image.getMetadataURL()).text)
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
    return image.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, zoom)
  }

  static Annotation getAnnotationFromData(data) {
    def annotation = new Annotation()
    annotation.name = data.annotation.name
    annotation.location = new WKTReader().read(data.annotation.location);
    annotation.image = Image.get(data.annotation.image);
    annotation.zoomLevel = data.annotation.zoomLevel!=null ? ((String)data.annotation.zoomLevel).toDouble() : 0
    annotation.channels =  data.annotation.channels
    annotation.created = data.annotation.created
    annotation.updated = data.annotation.updated
    annotation.deleted = data.annotation.deleted
    return annotation;
  }


  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Annotation.class
    JSON.registerObjectMarshaller(Annotation) {
      def returnArray = [:]
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['location'] = it.location.toString()
      if(it.image!=null) returnArray['image'] = it.image.id
      return returnArray
    }
  }
}
