package be.cytomine.ontology

import grails.converters.*
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.security.User
import be.cytomine.SequenceDomain
import be.cytomine.rest.UrlApi

import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import be.cytomine.image.ImageInstance

class Annotation extends SequenceDomain implements Serializable {

  String name
  Geometry location
  ImageInstance image
  Double zoomLevel
  String channels
  User user

  static belongsTo = [ImageInstance]
  static hasMany = [ annotationTerm: AnnotationTerm ]

  static transients = ["cropURL", "boundaries"]

  static constraints = {
    name(blank:true)
    location(nullable:false)
    zoomLevel(nullable:true)
    channels(nullable:true)
    user(nullable:false)
  }

  static mapping = {
    id generator : "assigned"
    columns {
      location type: org.hibernatespatial.GeometryUserType
    }
  }

  String toString() {return "Annotation " + id}

  /**
   * If name is empty, fill it by "Annotation $id"
   */
  public beforeInsert() {
    super.beforeInsert()
    name = name && !name.trim().equals("")? name : "Annotation " + id
  }

  /**
   * Get all terms map with the annotation
   * @return list of terms
   */
  def terms() {
    return annotationTerm.collect{
      it.term
    }
  }

  def project() {
    return image?.project
  }

  private def getArea() {
    //TODO: must be compute with zoom level
    return location.area
  }

  private def getPerimeter() {
    //TODO: must be compute with zoom level
    return location.getLength()
  }

  private def getBoundaries () {
    /*def metadata = JSON.parse(new URL(image.getMetadataURL()).text)
    int zoom = Integer.parseInt(metadata.levels)*/
    Coordinate[] coordinates = location.getEnvelope().getCoordinates()
    int topLeftX = coordinates[3].x
    int topLeftY = coordinates[3].y
    //int topLeftY = Integer.parseInt(metadata.height) - coordinates[3].y
    int width =  coordinates[1].x - coordinates[0].x
    int height =  coordinates[3].y - coordinates[0].y

    log.debug "topLeftX :"+ topLeftX + " topLeftY :" + topLeftY + " width :" +  width + " height :" + height
    return [topLeftX : topLeftX, topLeftY : topLeftY,width : width, height : height]
  }

  def getCropURL() {
    def boundaries = getBoundaries()
    return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
  }

  def getCropURL(int zoom) {
    def boundaries = getBoundaries()
    return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, zoom)
  }

  /**
   * Create a new Annotation with jsonAnnotation attributes
   * So, jsonAnnotation must have jsonAnnotation.location, jsonAnnotation.name, ...
   * @param jsonAnnotation JSON
   * @return Annotation
   */
  static Annotation createAnnotationFromData(jsonAnnotation) {
    def annotation = new Annotation()
    getAnnotationFromData(annotation,jsonAnnotation)
  }

  /**
   * Fill annotation with data attributes
   * So, jsonAnnotation must have jsonAnnotation.location, jsonAnnotation.name, ...
   * @param annotation Annotation Source
   * @param jsonAnnotation JSON
   * @return annotation with json attributes
   */
  static Annotation getAnnotationFromData(annotation,jsonAnnotation) {
    annotation.name = jsonAnnotation.name
    annotation.location = new WKTReader().read(jsonAnnotation.location);
    //annotation.location = DouglasPeuckerSimplifier.simplify(annotation.location,50)
    annotation.image = ImageInstance.get(jsonAnnotation.image);
    annotation.zoomLevel = (!jsonAnnotation.zoomLevel.toString().equals("null"))  ? ((String)jsonAnnotation.zoomLevel).toDouble() : -1
    annotation.channels =  jsonAnnotation.channels
    annotation.user =  User.get(jsonAnnotation.user);

    annotation.created = (!jsonAnnotation.created.toString().equals("null"))  ? new Date(Long.parseLong(jsonAnnotation.created)) : null
    annotation.updated = (!jsonAnnotation.updated.toString().equals("null"))  ? new Date(Long.parseLong(jsonAnnotation.updated)) : null

    return annotation;
  }



  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Annotation.class
    JSON.registerObjectMarshaller(Annotation) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name!=""? it.name : "Annotation " + it.id
      returnArray['location'] = it.location.toString()
      returnArray['image'] = it.image? it.image.id : null
      returnArray['zoomLevel'] = it.zoomLevel
      returnArray['channels'] = it.channels
      returnArray['user'] = it.user? it.user.id : null

      returnArray['area'] = it.getArea()
      returnArray['perimeter'] = it.getPerimeter()


      returnArray['created'] = it.created? it.created.time.toString() : null
      returnArray['updated'] = it.updated? it.updated.time.toString() : null

      returnArray['term'] = UrlApi.getTermsURLWithAnnotationId(it.id)


      String termList = "";
      if(it.terms()!=null)
      {
          def termName = []
          it.terms().each{ term ->
             termName << term.name
          }
        termList = termName.join(',')
        if(termList.size()>28)
          termList = termList.substring(0,25) + "..."
      }
      returnArray['termList'] =  termList

      returnArray['cropURL'] = UrlApi.getAnnotationCropWithAnnotationId(it.id)

      return returnArray
    }
  }

}
