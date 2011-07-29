package be.cytomine.ontology

import grails.converters.*
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.security.User
import be.cytomine.SequenceDomain
import be.cytomine.rest.UrlApi

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project

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
    /*public afterInsert() {
      println "Annotation.afterInsert"
        Project project = image.project;
        project.countAnnotations++
    }
    public def afterDelete()  {
      println "Annotation.afterDelete"
        Project project = image.project;
        project.countAnnotations--
    }*/
    /**
     * Get all terms map with the annotation
     * @return list of terms
     */
    def terms() {
        return annotationTerm.collect{
            it.term
        }
    }
    def termsId() {
        return annotationTerm.collect{
            it.getIdTerm()
        }
    }

    def project() {
        return image?.project
    }

    String imageFileName() {
        return this.image?.baseImage?.getFilename()
    }

    private def getArea() {
        //TODO: must be compute with zoom level
        return location.area
    }

    private def getPerimeter() {
        //TODO: must be compute with zoom level
        return location.getLength()
    }

    private def getCentroid() {
        if (location.area < 1) return null
        def centroid = location.getCentroid()
        def response = [:]
        response.x = centroid.x
        response.y = centroid.y
        return response
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
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
    }

    def computeArea() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getArea()) + " pixels²"
        else return Math.round(this.getArea() * this.image.baseImage.resolution)  + " µm²"
    }

    def computePerimeter() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getPerimeter()) + " pixels"
        else return Math.round(this.getPerimeter() * this.image.baseImage.resolution)  + " µm"
    }
    /**
     * Create a new Annotation with jsonAnnotation attributes
     * So, jsonAnnotation must have jsonAnnotation.location, jsonAnnotation.name, ...
     * @param jsonAnnotation JSON
     * @return Annotation
     */
    static Annotation createFromData(jsonAnnotation) {
        def annotation = new Annotation()
        getFromData(annotation,jsonAnnotation)
    }

    /**
     * Fill annotation with data attributes
     * So, jsonAnnotation must have jsonAnnotation.location, jsonAnnotation.name, ...
     * @param annotation Annotation Source
     * @param jsonAnnotation JSON
     * @return annotation with json attributes
     */
    static Annotation getFromData(annotation,jsonAnnotation) {
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

    def getIdImage() {
         if(this.imageId) return this.imageId
         else return this.image?.id
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Annotation.class
        JSON.registerObjectMarshaller(Annotation) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name!=""? it.name : "Annotation " + it.id
            returnArray['location'] = it.location.toString()
            returnArray['image'] = it.getIdImage()
            returnArray['imageFilename'] = it.image? it.image.baseImage.filename : null
            returnArray['zoomLevel'] = it.zoomLevel
            returnArray['channels'] = it.channels
            if(it.userId) returnArray['user'] = it.userId
            else returnArray['user'] = it.user?.id

            returnArray['area'] = it.computeArea()
            returnArray['perimeter'] = it.computePerimeter()
            returnArray['centroid'] = it.getCentroid()

            returnArray['created'] = it.created? it.created.time.toString() : null
            returnArray['updated'] = it.updated? it.updated.time.toString() : null

            returnArray['term'] = it.termsId()

            returnArray['cropURL'] = UrlApi.getAnnotationCropWithAnnotationId(it.id)
            //returnArray['cropURL'] = it.getCropURL()

            return returnArray
        }
    }

}
