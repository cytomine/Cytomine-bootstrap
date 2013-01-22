package be.cytomine

import com.vividsolutions.jts.geom.Geometry
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import be.cytomine.project.Project
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.AlgoAnnotationTerm
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.Exception.WrongArgumentException
import org.apache.log4j.Logger
import grails.converters.JSON
import be.cytomine.api.UrlApi
import be.cytomine.ontology.Term
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ontology.UserAnnotation
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation

/**
 * User: lrollus
 * Date: 18/10/12
 * GIGA-ULg
 *
 * Annotation generic domain
 * Annotation can be:
 * -UserAnnotation => created by human user
 * -AlgoAnnotation => created by job
 * -ReviewedAnnotation => User or AlgoAnnotation validate by user
 */
abstract class AnnotationDomain extends CytomineDomain implements Serializable {

    /**
     * Annotation geometry object
     */
    Geometry location

    /**
     * Annotation image
     */
    ImageInstance image

    /**
     * Annotation project
     * Redundant with image.project, speedup
     */
    Project project

    /**
     * Compression threshold used for annotation simplification
     */
    Double geometryCompression

    /**
     * Number of comments for annotation
     * Redundant to speed up
     */
    long countComments = 0L

    /**
     * Annotation geometry WKT location
     * Redundant, better to use this than getting WKT from location properties
     */
    String wktLocation  //speedup listing

    /* Transients values for JSON/XML rendering */
    //TODO:: remove from here, use custom SQL request with these info
    Double similarity
    Double rate
    Long idTerm
    Long idExpectedTerm


    static belongsTo = [ImageInstance, Project]

    static transients = ["boundaries", "similarity","rate", "idTerm", "idExpectedTerm"]

    static constraints = {
        location(nullable: false)
        geometryCompression(nullable: true)
        project(nullable:true)
        wktLocation(nullable:false, empty:false)
    }

    static mapping = {
        wktLocation(type: 'text')
        tablePerHierarchy false
        id generator: "assigned"
        columns {
            location type: org.hibernatespatial.GeometryUserType
        }
    }

    /**
     * If name is empty, fill it by "Annotation $id"
     */
    public beforeInsert() {
        super.beforeInsert()
        project = image.project
        if(!wktLocation)
            wktLocation = location.toText()
    }

    def beforeUpdate() {
        super.beforeUpdate()
        wktLocation = location.toText()
    }

    def beforeValidate() {
        if (!created) {
            created = new Date()
        }
        if (id == null) {
            id = sequenceService.generateID()
        }

        if(!wktLocation)
            wktLocation = location.toText()
    }

    /**
     * Get all terms map with the annotation
     * @return Terms list
     */
    abstract def terms()

    /**
     * Get all annotation terms id
     * @return Terms id list
     */
    abstract def termsId()

    /**
     * Check if its an algo annotation
     */
    abstract boolean isAlgoAnnotation()

    /**
     * Check if its a review annotation
     */
    abstract boolean isReviewedAnnotation()

    /**
     * Get all terms for automatic review
     * If review is done "for all" (without manual user control), we add these term to the new review annotation
     * @return
     */
    abstract List<Term> termsForReview()

    /**
     * Get CROP (annotation image area) URL for this annotation
     * @param cytomineUrl Cytomine base URL
     * @return Full CROP Url
     */
    abstract def getCropUrl(String cytomineUrl)

    String toString() {return "Annotation " + id}

    def getFilename() {
          return this.image?.baseImage?.getFilename()
      }

    Project projectDomain() {
        return image?.project
    }

    def getArea() {
        //TODO: must be compute with zoom level
        return location.area
    }

    def getPerimeter() {
        //TODO: must be compute with zoom level
        return location.getLength()
    }

    def getCentroid() {
        if (location.area < 1) return null
        def centroid = location.getCentroid()
        def response = [:]
        response.x = centroid.x
        response.y = centroid.y
        return response
    }

   def getBoundaries() {
       //get num points
       if (location.getNumPoints()>3) {
           Coordinate[] coordinates = location.getEnvelope().getCoordinates()
           int topLeftX = coordinates[3].x
           int topLeftY = coordinates[3].y
           //int topLeftY = Integer.parseInt(metadata.height) - coordinates[3].y
           int width = coordinates[1].x - coordinates[0].x
           int height = coordinates[3].y - coordinates[0].y

           //log.debug "topLeftX :" + topLeftX + " topLeftY :" + topLeftY + " width :" + width + " height :" + height
           return [topLeftX: topLeftX, topLeftY: topLeftY, width: width, height: height]
       } else throw new be.cytomine.Exception.InvalidRequestException("Cannot make a crop for a POINT")

    }

    def toCropURL() {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
    }

    def toCropURLWithMaxSize(int maxSize) {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURLWithMaxWithOrHeight(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, maxSize, maxSize)
    }

    def toCropURL(int zoom) {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, zoom)
    }

    def computeArea() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getArea())// + " pixels²"
        else return Math.round(this.getArea() * this.image.baseImage.resolution)// + " µm²"
    }

    def computePerimeter() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getPerimeter())// + " pixels"
        else return Math.round(this.getPerimeter() * this.image.baseImage.resolution)// + " µm"
    }

    def getCallBack() {
        return [annotationID: this.id, imageID: this.image.id]

    }


    /**
     * Get user/algo/reviewed annotation with id
     * Check the correct type and return it
     * @param id Annotation id
     * @return Annotation
     */
    public static AnnotationDomain getAnnotationDomain(String id) {
        try {
            getAnnotationDomain(Long.parseLong(id))
        } catch(NumberFormatException e) {
            throw new ObjectNotFoundException("Annotation ${id} not found")
        }
    }

    /**
     * Get user/algo/reviewed annotation with id
     * Check the correct type and return it
     * @param id Annotation id
     * @return Annotation
     */
    public static AnnotationDomain getAnnotationDomain(long id) {
        AnnotationDomain basedAnnotation = UserAnnotation.read(id)
        if (!basedAnnotation)
            basedAnnotation = AlgoAnnotation.read(id)
        if (!basedAnnotation)
            basedAnnotation = ReviewedAnnotation.read(id)
        if (basedAnnotation) return basedAnnotation
        else throw new ObjectNotFoundException("Annotation ${id} not found")

    }

}
